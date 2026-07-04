package com.forgemind.projects.dto;

import com.forgemind.projects.entity.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateProjectRequest(

        UUID workspaceId,

        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must be less than 255 characters")
        String name,

        @Size(max = 5000, message = "Description must be less than 5000 characters")
        String description,

        ProjectVisibility visibility,

        @Size(max = 20, message = "A project can have at most 20 tags")
        List<@Size(max = 64, message = "Tag must be less than 64 characters") String> tags
) {
}