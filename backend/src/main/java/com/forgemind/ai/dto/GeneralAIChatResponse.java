package com.forgemind.ai.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record GeneralAIChatResponse(
        UUID conversationId, // Added this
        String title,        // Added this
        AIMessageResponse userMessage,      // Switched to full message tracking
        AIMessageResponse assistantMessage  // Switched to full message tracking
) {
}