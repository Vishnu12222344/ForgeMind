package com.forgemind.ai.dto;

import com.forgemind.ai.entity.AIMessageRole;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record AIMessageResponse(
        UUID id,
        AIMessageRole role,
        String content,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        List<ReferencedFileResponse> referencedFiles,
        Instant createdAt
) {
}