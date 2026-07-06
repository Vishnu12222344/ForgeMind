package com.forgemind.documentation.dto;

import com.forgemind.documentation.entity.DocumentationType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record GeneratedDocumentSummaryResponse(
        UUID id,
        UUID projectId,
        DocumentationType type,
        String title,
        long totalTokens,
        Instant createdAt,
        Instant updatedAt
) {
}