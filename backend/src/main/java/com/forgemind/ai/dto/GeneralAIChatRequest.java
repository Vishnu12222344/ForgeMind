package com.forgemind.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GeneralAIChatRequest(
        UUID conversationId,
        @NotBlank(message = "Message is required")
        @Size(max = 10000, message = "Message must be less than 10000 characters")
        String message
) {
}