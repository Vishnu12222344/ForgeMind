package com.forgemind.ai.provider;

import lombok.Builder;

import java.util.List;

@Builder
public record AIProviderResponse(
        String content,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        List<AIContextFile> referencedFiles
) {
}