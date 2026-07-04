package com.forgemind.projects.dto;

import com.forgemind.projects.entity.ProjectVisibility;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record ProjectResponse(
        UUID id,
        UUID workspaceId,
        UUID createdById,
        String name,
        String slug,
        String description,
        ProjectVisibility visibility,
        boolean archived,
        boolean favorite,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
}