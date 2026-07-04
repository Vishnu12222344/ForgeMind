package com.forgemind.repositories.dto;

import com.forgemind.repositories.entity.RepositoryFileType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RepositoryFileResponse(
        UUID id,
        UUID repositoryId,
        RepositoryFileType type,
        String path,
        String name,
        String extension,
        String language,
        long sizeBytes,
        int depth,
        boolean binaryFile,
        boolean contentTruncated,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}