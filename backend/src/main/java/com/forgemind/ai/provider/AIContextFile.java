package com.forgemind.ai.provider;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AIContextFile(
        UUID id,
        String path,
        String name,
        String language,
        String content
) {
}