package com.forgemind.repositories.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.common.exception.BadRequestException;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.projects.entity.Project;
import com.forgemind.projects.entity.ProjectVisibility;
import com.forgemind.projects.mapper.ProjectMapper;
import com.forgemind.projects.service.ProjectService;
import com.forgemind.repositories.dto.*;
import com.forgemind.repositories.entity.RepositoryFile;
import com.forgemind.repositories.entity.RepositoryFileType;
import com.forgemind.repositories.entity.RepositoryStatus;
import com.forgemind.repositories.entity.SourceRepository;
import com.forgemind.repositories.mapper.RepositoryMapper;
import com.forgemind.repositories.parser.ParsedRepositoryResult;
import com.forgemind.repositories.parser.ZipRepositoryParser;
import com.forgemind.repositories.repository.RepositoryFileRepository;
import com.forgemind.repositories.repository.SourceRepositoryRepository;
import com.forgemind.users.entity.User;
import com.forgemind.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final SourceRepositoryRepository sourceRepositoryRepository;
    private final RepositoryFileRepository repositoryFileRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ZipRepositoryParser zipRepositoryParser;
    private final GitHubRepositoryImportService gitHubRepositoryImportService;
    private final ObjectMapper objectMapper;

    @Value("${app.repository.max-upload-size-mb:100}")
    private long maxUploadSizeMb;

    @Transactional
    public RepositoryResponse uploadRepository(
            UUID userId,
            UUID projectId,
            MultipartFile file,
            boolean replaceExisting
    ) {
        validateUpload(file);

        Project project = projectService.getProjectEntityForUser(userId, projectId);
        User user = userService.findById(userId);

        Optional<SourceRepository> existingRepository = sourceRepositoryRepository.findByProject_Id(projectId);

        if (existingRepository.isPresent()) {
            if (!replaceExisting) {
                throw new BadRequestException("Project already has a repository");
            }

            repositoryFileRepository.deleteByRepository_Id(existingRepository.get().getId());
            sourceRepositoryRepository.delete(existingRepository.get());
            sourceRepositoryRepository.flush();
        }

        SourceRepository repository = SourceRepository.builder()
                .project(project)
                .uploadedBy(user)
                .originalFilename(Objects.requireNonNull(file.getOriginalFilename()))
                .status(RepositoryStatus.PARSING)
                .totalFiles(0)
                .totalFolders(0)
                .totalSizeBytes(0)
                .primaryLanguage(null)
                .languageStatsJson("{}")
                .parseError(null)
                .parsedAt(null)
                .build();

        repository = sourceRepositoryRepository.save(repository);

        try {
            ParsedRepositoryResult result = zipRepositoryParser.parse(file, repository);

            repository.setStatus(RepositoryStatus.READY);
            repository.setTotalFiles(result.totalFiles());
            repository.setTotalFolders(result.totalFolders());
            repository.setTotalSizeBytes(result.totalSizeBytes());
            repository.setPrimaryLanguage(result.primaryLanguage());
            repository.setLanguageStatsJson(toJson(result.languageStats()));
            repository.setParsedAt(Instant.now());

            SourceRepository savedRepository = sourceRepositoryRepository.save(repository);

            return RepositoryMapper.toRepositoryResponse(savedRepository, result.languageStats());
        } catch (RuntimeException ex) {
            repository.setStatus(RepositoryStatus.FAILED);
            repository.setParseError(ex.getMessage());
            sourceRepositoryRepository.save(repository);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public RepositoryResponse getRepository(UUID userId, UUID projectId) {
        projectService.getProjectEntityForUser(userId, projectId);

        SourceRepository repository = sourceRepositoryRepository.findByProject_Id(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));

        return RepositoryMapper.toRepositoryResponse(
                repository,
                parseLanguageStats(repository.getLanguageStatsJson())
        );
    }

    @Transactional(readOnly = true)
    public List<RepositoryTreeNodeResponse> getRepositoryTree(UUID userId, UUID projectId) {
        SourceRepository repository = getRepositoryEntityForUser(userId, projectId);

        List<RepositoryFile> files = repositoryFileRepository.findByRepository_IdOrderByPathAsc(repository.getId());

        return buildTree(files);
    }
    @Transactional
    public RepositoryImportResponse uploadRepositoryWithoutProject(
            UUID userId,
            MultipartFile file,
            String projectName
    ) {
        validateUpload(file);

        String resolvedProjectName = projectName != null && !projectName.isBlank()
                ? projectName.trim()
                : resolveProjectNameFromFilename(file.getOriginalFilename());

        Project project = projectService.createProjectEntityForUser(
                userId,
                resolvedProjectName,
                "Project automatically created from ZIP upload.",
                ProjectVisibility.PRIVATE,
                List.of("zip-upload")
        );

        User user = userService.findById(userId);

        SourceRepository repository = createRepositoryShell(
                project,
                user,
                Objects.requireNonNull(file.getOriginalFilename())
        );

        try {
            ParsedRepositoryResult result = zipRepositoryParser.parse(file, repository);
            RepositoryResponse repositoryResponse = finalizeRepository(repository, result);

            return RepositoryImportResponse.builder()
                    .project(ProjectMapper.toResponse(project))
                    .repository(repositoryResponse)
                    .build();

        } catch (RuntimeException ex) {
            markRepositoryFailed(repository, ex);
            throw ex;
        }
    }

    @Transactional
    public RepositoryImportResponse importGitHubRepository(
            UUID userId,
            String repoUrl,
            String branch
    ) {
        GitHubDownloadedRepository downloadedRepository =
                gitHubRepositoryImportService.downloadRepository(repoUrl, branch);

        Project project = projectService.createProjectEntityForUser(
                userId,
                downloadedRepository.repoName(),
                downloadedRepository.description() != null
                        ? downloadedRepository.description()
                        : "Project automatically imported from GitHub: " + downloadedRepository.fullName(),
                ProjectVisibility.PRIVATE,
                List.of("github", downloadedRepository.owner())
        );

        User user = userService.findById(userId);

        SourceRepository repository = createRepositoryShell(
                project,
                user,
                downloadedRepository.repoName() + "-" + downloadedRepository.branch() + ".zip"
        );

        try {
            InputStream inputStream = new ByteArrayInputStream(downloadedRepository.zipBytes());

            ParsedRepositoryResult result = zipRepositoryParser.parse(inputStream, repository);
            RepositoryResponse repositoryResponse = finalizeRepository(repository, result);

            return RepositoryImportResponse.builder()
                    .project(ProjectMapper.toResponse(project))
                    .repository(repositoryResponse)
                    .build();

        } catch (RuntimeException ex) {
            markRepositoryFailed(repository, ex);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public RepositoryFileResponse getFile(UUID userId, UUID projectId, UUID fileId) {
        SourceRepository repository = getRepositoryEntityForUser(userId, projectId);

        RepositoryFile file = repositoryFileRepository.findByIdAndRepository_Id(fileId, repository.getId())
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (file.getType() == RepositoryFileType.FOLDER) {
            throw new BadRequestException("Requested path is a folder, not a file");
        }

        return RepositoryMapper.toFileResponse(file);
    }

    @Transactional
    public void deleteRepository(UUID userId, UUID projectId) {
        SourceRepository repository = getRepositoryEntityForUser(userId, projectId);

        repositoryFileRepository.deleteByRepository_Id(repository.getId());
        sourceRepositoryRepository.delete(repository);
    }

    private SourceRepository createRepositoryShell(
            Project project,
            User user,
            String originalFilename
    ) {
        SourceRepository repository = SourceRepository.builder()
                .project(project)
                .uploadedBy(user)
                .originalFilename(originalFilename)
                .status(RepositoryStatus.PARSING)
                .totalFiles(0)
                .totalFolders(0)
                .totalSizeBytes(0)
                .primaryLanguage(null)
                .languageStatsJson("{}")
                .parseError(null)
                .parsedAt(null)
                .build();

        return sourceRepositoryRepository.save(repository);
    }

    private RepositoryResponse finalizeRepository(
            SourceRepository repository,
            ParsedRepositoryResult result
    ) {
        repository.setStatus(RepositoryStatus.READY);
        repository.setTotalFiles(result.totalFiles());
        repository.setTotalFolders(result.totalFolders());
        repository.setTotalSizeBytes(result.totalSizeBytes());
        repository.setPrimaryLanguage(result.primaryLanguage());
        repository.setLanguageStatsJson(toJson(result.languageStats()));
        repository.setParsedAt(Instant.now());

        SourceRepository savedRepository = sourceRepositoryRepository.save(repository);

        return RepositoryMapper.toRepositoryResponse(savedRepository, result.languageStats());
    }

    private void markRepositoryFailed(SourceRepository repository, RuntimeException ex) {
        repository.setStatus(RepositoryStatus.FAILED);
        repository.setParseError(ex.getMessage());
        sourceRepositoryRepository.save(repository);
    }

    private String resolveProjectNameFromFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "Uploaded Repository";
        }

        String cleaned = filename.trim();

        if (cleaned.toLowerCase().endsWith(".zip")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }

        cleaned = cleaned.replaceAll("[_-]+", " ").trim();

        return cleaned.isBlank() ? "Uploaded Repository" : cleaned;
    }

    private SourceRepository getRepositoryEntityForUser(UUID userId, UUID projectId) {
        projectService.getProjectEntityForUser(userId, projectId);

        return sourceRepositoryRepository.findByProject_Id(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("ZIP file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
            throw new BadRequestException("Only ZIP files are allowed");
        }

        long maxBytes = maxUploadSizeMb * 1024 * 1024;

        if (file.getSize() > maxBytes) {
            throw new BadRequestException("ZIP file exceeds maximum upload size of " + maxUploadSizeMb + " MB");
        }
    }

    private String toJson(Map<String, Long> map) {
        try {
            return objectMapper.writeValueAsString(map == null ? Map.of() : map);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Long> parseLanguageStats(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private List<RepositoryTreeNodeResponse> buildTree(List<RepositoryFile> files) {
        Map<String, MutableTreeNode> nodeMap = new HashMap<>();

        MutableTreeNode root = new MutableTreeNode(
                null,
                "",
                "",
                RepositoryFileType.FOLDER,
                null,
                null,
                0,
                false,
                false
        );

        nodeMap.put("", root);

        for (RepositoryFile file : files) {
            MutableTreeNode node = new MutableTreeNode(
                    file.getId(),
                    file.getName(),
                    file.getPath(),
                    file.getType(),
                    file.getLanguage(),
                    file.getExtension(),
                    file.getSizeBytes(),
                    file.isBinaryFile(),
                    file.isContentTruncated()
            );

            nodeMap.put(file.getPath(), node);
        }

        for (RepositoryFile file : files) {
            MutableTreeNode node = nodeMap.get(file.getPath());
            String parentPath = getParentPath(file.getPath());

            MutableTreeNode parent = nodeMap.get(parentPath);

            if (parent == null) {
                parent = createVirtualParent(parentPath, nodeMap);
            }

            parent.children.add(node);
        }

        return root.children
                .stream()
                .sorted(this::compareNodes)
                .map(this::toTreeResponse)
                .toList();
    }

    private MutableTreeNode createVirtualParent(String path, Map<String, MutableTreeNode> nodeMap) {
        if (path == null || path.isBlank()) {
            return nodeMap.get("");
        }

        MutableTreeNode existing = nodeMap.get(path);

        if (existing != null) {
            return existing;
        }

        MutableTreeNode node = new MutableTreeNode(
                null,
                getNameFromPath(path),
                path,
                RepositoryFileType.FOLDER,
                null,
                null,
                0,
                false,
                false
        );

        nodeMap.put(path, node);

        String parentPath = getParentPath(path);
        MutableTreeNode parent = createVirtualParent(parentPath, nodeMap);
        parent.children.add(node);

        return node;
    }

    private RepositoryTreeNodeResponse toTreeResponse(MutableTreeNode node) {
        return RepositoryTreeNodeResponse.builder()
                .id(node.id)
                .name(node.name)
                .path(node.path)
                .type(node.type)
                .language(node.language)
                .extension(node.extension)
                .sizeBytes(node.sizeBytes)
                .binaryFile(node.binaryFile)
                .contentTruncated(node.contentTruncated)
                .children(
                        node.children
                                .stream()
                                .sorted(this::compareNodes)
                                .map(this::toTreeResponse)
                                .toList()
                )
                .build();
    }

    private int compareNodes(MutableTreeNode a, MutableTreeNode b) {
        if (a.type != b.type) {
            return a.type == RepositoryFileType.FOLDER ? -1 : 1;
        }

        return a.name.compareToIgnoreCase(b.name);
    }

    private String getParentPath(String path) {
        if (path == null || path.isBlank() || !path.contains("/")) {
            return "";
        }

        return path.substring(0, path.lastIndexOf('/'));
    }

    private String getNameFromPath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }

        int index = path.lastIndexOf('/');

        if (index < 0) {
            return path;
        }

        return path.substring(index + 1);
    }

    private static class MutableTreeNode {
        private final UUID id;
        private final String name;
        private final String path;
        private final RepositoryFileType type;
        private final String language;
        private final String extension;
        private final long sizeBytes;
        private final boolean binaryFile;
        private final boolean contentTruncated;
        private final List<MutableTreeNode> children = new ArrayList<>();

        private MutableTreeNode(
                UUID id,
                String name,
                String path,
                RepositoryFileType type,
                String language,
                String extension,
                long sizeBytes,
                boolean binaryFile,
                boolean contentTruncated
        ) {
            this.id = id;
            this.name = name;
            this.path = path;
            this.type = type;
            this.language = language;
            this.extension = extension;
            this.sizeBytes = sizeBytes;
            this.binaryFile = binaryFile;
            this.contentTruncated = contentTruncated;
        }
    }
}