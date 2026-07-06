package com.forgemind.documentation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.ai.provider.AIContextFile;
import com.forgemind.projects.entity.Project;
import com.forgemind.repositories.entity.RepositoryFile;
import com.forgemind.repositories.entity.RepositoryFileType;
import com.forgemind.repositories.entity.SourceRepository;
import com.forgemind.repositories.repository.RepositoryFileRepository;
import com.forgemind.repositories.repository.SourceRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentationContextService {

    private final SourceRepositoryRepository sourceRepositoryRepository;
    private final RepositoryFileRepository repositoryFileRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public DocumentationContext buildContext(Project project) {
        Optional<SourceRepository> repositoryOptional =
                sourceRepositoryRepository.findByProject_Id(project.getId());

        if (repositoryOptional.isEmpty()) {
            return new DocumentationContext(
                    false,
                    null,
                    null,
                    0,
                    0,
                    0,
                    Map.of(),
                    "No repository has been uploaded for this project.",
                    List.of()
            );
        }

        SourceRepository repository = repositoryOptional.get();

        List<RepositoryFile> files =
                repositoryFileRepository.findByRepository_IdOrderByPathAsc(repository.getId());

        String tree = buildRepositoryTree(files);
        List<AIContextFile> importantFiles = selectImportantFiles(files);

        return new DocumentationContext(
                true,
                repository.getOriginalFilename(),
                repository.getPrimaryLanguage(),
                repository.getTotalFiles(),
                repository.getTotalFolders(),
                repository.getTotalSizeBytes(),
                parseLanguageStats(repository.getLanguageStatsJson()),
                tree,
                importantFiles
        );
    }

    private String buildRepositoryTree(List<RepositoryFile> files) {
        if (files == null || files.isEmpty()) {
            return "Repository tree is empty.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Repository tree:\n\n");

        files.stream()
                .sorted(Comparator.comparing(RepositoryFile::getPath))
                .limit(80)
                .forEach(file -> {
                    int depth = Math.max(0, file.getDepth());
                    builder.append("  ".repeat(depth));

                    if (file.getType() == RepositoryFileType.FOLDER) {
                        builder.append("📁 ");
                    } else {
                        builder.append("📄 ");
                    }

                    builder.append(file.getPath());

                    if (file.getLanguage() != null) {
                        builder.append(" [").append(file.getLanguage()).append("]");
                    }

                    builder.append("\n");
                });

        if (files.size() > 350) {
            builder.append("\n... tree truncated. Total entries: ")
                    .append(files.size())
                    .append("\n");
        }

        return builder.toString();
    }

    private List<AIContextFile> selectImportantFiles(List<RepositoryFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .filter(file -> file.getType() == RepositoryFileType.FILE)
                .filter(file -> !file.isBinaryFile())
                .filter(file -> file.getContent() != null && !file.getContent().isBlank())
                .sorted(Comparator.comparingInt(this::scoreFile).reversed())
                .limit(3)
                .map(file -> AIContextFile.builder()
                        .id(file.getId())
                        .path(file.getPath())
                        .name(file.getName())
                        .language(file.getLanguage())
                        .content(file.getContent())
                        .build()
                )
                .toList();
    }

    private int scoreFile(RepositoryFile file) {
        String path = file.getPath().toLowerCase();
        String name = file.getName().toLowerCase();

        int score = 0;

        if (name.equals("readme.md")) score += 100;
        if (name.equals("pom.xml")) score += 95;
        if (name.equals("package.json")) score += 95;
        if (name.equals("build.gradle")) score += 90;
        if (name.equals("settings.gradle")) score += 80;
        if (name.equals("dockerfile")) score += 80;
        if (name.equals("docker-compose.yml")) score += 80;
        if (name.equals("application.yml")) score += 85;
        if (name.equals("application.properties")) score += 85;

        if (path.contains("/controller/")) score += 70;
        if (path.contains("/service/")) score += 65;
        if (path.contains("/entity/")) score += 60;
        if (path.contains("/repository/")) score += 55;
        if (path.contains("/config/")) score += 50;
        if (path.contains("/security/")) score += 50;

        if (path.contains("/pages/")) score += 45;
        if (path.contains("/components/")) score += 40;
        if (path.contains("/routes/")) score += 40;
        if (path.contains("/src/main/")) score += 30;

        if (file.getSizeBytes() > 80_000) score -= 20;

        return score;
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
}