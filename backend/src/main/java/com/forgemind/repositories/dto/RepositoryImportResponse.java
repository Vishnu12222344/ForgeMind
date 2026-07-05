package com.forgemind.repositories.dto;

import com.forgemind.projects.dto.ProjectResponse;
import lombok.Builder;

@Builder
public record RepositoryImportResponse(
        ProjectResponse project,
        RepositoryResponse repository
) {
}