package com.forgemind.repositories.dto;

import com.forgemind.repositories.entity.RepositoryFileType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RepositoryTreeNodeResponse(
        UUID id,
        String name,
        String path,
        RepositoryFileType type,
        String language,
        String extension,
        long sizeBytes,
        boolean binaryFile,
        boolean contentTruncated,
        List<RepositoryTreeNodeResponse> children
) {
}