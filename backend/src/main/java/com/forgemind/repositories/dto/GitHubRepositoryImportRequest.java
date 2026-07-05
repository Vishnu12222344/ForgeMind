package com.forgemind.repositories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GitHubRepositoryImportRequest(

        @NotBlank(message = "GitHub repository URL is required")
        @Size(max = 1000)
        String repoUrl,

        @Size(max = 255)
        String branch
) {
}