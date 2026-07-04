package com.forgemind.projects.service;

import com.forgemind.common.exception.BadRequestException;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.common.util.SlugUtils;
import com.forgemind.projects.dto.CreateProjectRequest;
import com.forgemind.projects.dto.ProjectResponse;
import com.forgemind.projects.dto.UpdateProjectRequest;
import com.forgemind.projects.entity.Project;
import com.forgemind.projects.entity.ProjectVisibility;
import com.forgemind.projects.mapper.ProjectMapper;
import com.forgemind.projects.repository.ProjectRepository;
import com.forgemind.projects.repository.ProjectSpecifications;
import com.forgemind.users.entity.User;
import com.forgemind.users.service.UserService;
import com.forgemind.workspaces.entity.Workspace;
import com.forgemind.workspaces.repository.WorkspaceMemberRepository;
import com.forgemind.workspaces.repository.WorkspaceRepository;
import com.forgemind.workspaces.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceService workspaceService;
    private final UserService userService;

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        User user = userService.findById(userId);

        Workspace workspace = request.workspaceId() == null
                ? workspaceService.getPersonalWorkspace(userId)
                : findWorkspaceForUser(request.workspaceId(), userId);

        String projectName = sanitizeRequiredName(request.name());
        String slug = generateUniqueSlug(workspace.getId(), projectName, null);

        Project project = Project.builder()
                .workspace(workspace)
                .createdBy(user)
                .name(projectName)
                .slug(slug)
                .description(normalizeDescription(request.description()))
                .visibility(request.visibility() == null ? ProjectVisibility.PRIVATE : request.visibility())
                .archived(false)
                .favorite(false)
                .build();

        project.replaceTags(request.tags());

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.toResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(
            UUID userId,
            String search,
            Boolean archived,
            Boolean favorite,
            ProjectVisibility visibility,
            String tag,
            Pageable pageable
    ) {
        List<UUID> workspaceIds = workspaceMemberRepository.findByUser_Id(userId)
                .stream()
                .map(member -> member.getWorkspace().getId())
                .toList();

        Specification<Project> spec = Specification
                .where(ProjectSpecifications.belongsToWorkspaces(workspaceIds))
                .and(ProjectSpecifications.search(search))
                .and(ProjectSpecifications.archived(archived))
                .and(ProjectSpecifications.favorite(favorite))
                .and(ProjectSpecifications.visibility(visibility))
                .and(ProjectSpecifications.hasTag(tag));

        return projectRepository.findAll(spec, pageable)
                .map(ProjectMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID userId, UUID projectId) {
        Project project = findProjectForUser(projectId, userId);
        return ProjectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
        Project project = findProjectForUser(projectId, userId);

        if (request.name() != null) {
            String newName = sanitizeRequiredName(request.name());

            if (!Objects.equals(project.getName(), newName)) {
                project.setName(newName);
                project.setSlug(generateUniqueSlug(
                        project.getWorkspace().getId(),
                        newName,
                        project.getId()
                ));
            }
        }

        if (request.description() != null) {
            project.setDescription(normalizeDescription(request.description()));
        }

        if (request.visibility() != null) {
            project.setVisibility(request.visibility());
        }

        if (request.tags() != null) {
            project.replaceTags(request.tags());
        }

        Project savedProject = projectRepository.save(project);

        return ProjectMapper.toResponse(savedProject);
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        Project project = findProjectForUser(projectId, userId);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse archiveProject(UUID userId, UUID projectId) {
        Project project = findProjectForUser(projectId, userId);
        project.setArchived(true);
        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse unarchiveProject(UUID userId, UUID projectId) {
        Project project = findProjectForUser(projectId, userId);
        project.setArchived(false);
        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse favoriteProject(UUID userId, UUID projectId) {
        Project project = findProjectForUser(projectId, userId);
        project.setFavorite(true);
        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse unfavoriteProject(UUID userId, UUID projectId) {
        Project project = findProjectForUser(projectId, userId);
        project.setFavorite(false);
        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    private Workspace findWorkspaceForUser(UUID workspaceId, UUID userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        boolean member = workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, userId);

        if (!member) {
            throw new ResourceNotFoundException("Workspace not found");
        }

        return workspace;
    }

    private Project findProjectForUser(UUID projectId, UUID userId) {
        Project project = projectRepository.findByIdWithRelations(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UUID workspaceId = project.getWorkspace().getId();

        boolean member = workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, userId);

        if (!member) {
            throw new ResourceNotFoundException("Project not found");
        }

        return project;
    }

    private String generateUniqueSlug(UUID workspaceId, String name, UUID currentProjectId) {
        String baseSlug = SlugUtils.slugify(name);
        String slug = baseSlug;
        int suffix = 1;

        while (true) {
            Project existingProject = projectRepository.findByWorkspace_IdAndSlug(workspaceId, slug)
                    .orElse(null);

            if (existingProject == null) {
                return slug;
            }

            if (currentProjectId != null && existingProject.getId().equals(currentProjectId)) {
                return slug;
            }

            slug = baseSlug + "-" + suffix;
            suffix++;
        }
    }

    private String sanitizeRequiredName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Project name is required");
        }

        return name.trim();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}