package com.forgemind.repositories.dto;

import com.forgemind.repositories.entity.RepositoryStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
public record RepositoryResponse(
        UUID id,
        UUID projectId,
        UUID uploadedById,
        String originalFilename,
        RepositoryStatus status,
        long totalFiles,
        long totalFolders,
        long totalSizeBytes,
        String primaryLanguage,
        Map<String, Long> languageStats,
        String parseError,
        Instant parsedAt,
        Instant createdAt,
        Instant updatedAt
) {
}