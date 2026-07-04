package com.forgemind.projects.mapper;

import com.forgemind.projects.dto.ProjectResponse;
import com.forgemind.projects.entity.Project;
import com.forgemind.projects.entity.ProjectTag;

import java.util.Comparator;
import java.util.List;

public class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project project) {
        List<String> tags = project.getTags() == null
                ? List.of()
                : project.getTags()
                .stream()
                .map(ProjectTag::getName)
                .sorted(Comparator.naturalOrder())
                .toList();

        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .createdById(project.getCreatedBy().getId())
                .name(project.getName())
                .slug(project.getSlug())
                .description(project.getDescription())
                .visibility(project.getVisibility())
                .archived(project.isArchived())
                .favorite(project.isFavorite())
                .tags(tags)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}