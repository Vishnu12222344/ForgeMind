package com.forgemind.ai.dto;

import lombok.Builder;

@Builder
public record GeneralAIChatResponse(
        String content,
        long promptTokens,
        long completionTokens,
        long totalTokens
) {
}