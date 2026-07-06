package com.forgemind.documentation.dto;

import com.forgemind.documentation.entity.DocumentationType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record GeneratedDocumentResponse(
        UUID id,
        UUID projectId,
        UUID generatedById,
        DocumentationType type,
        String title,
        String content,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        Instant createdAt,
        Instant updatedAt
) {
}