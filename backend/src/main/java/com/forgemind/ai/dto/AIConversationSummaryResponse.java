package com.forgemind.ai.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AIConversationSummaryResponse(
        UUID id,
        UUID projectId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}