package com.forgemind.ai.provider;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record AIProviderRequest(
        UUID projectId,
        String projectName,
        String userMessage,
        List<AIContextFile> contextFiles
) {
}