package com.forgemind.ai.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AIChatResponse(
        UUID conversationId,
        String title,
        AIMessageResponse userMessage,
        AIMessageResponse assistantMessage
) {
}