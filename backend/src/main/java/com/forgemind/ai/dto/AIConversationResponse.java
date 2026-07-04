package com.forgemind.ai.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record AIConversationResponse(
        UUID id,
        UUID projectId,
        String title,
        List<AIMessageResponse> messages,
        Instant createdAt,
        Instant updatedAt
) {
}