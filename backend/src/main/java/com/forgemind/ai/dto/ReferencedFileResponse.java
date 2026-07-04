package com.forgemind.ai.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ReferencedFileResponse(
        UUID id,
        String path,
        String name,
        String language
) {
}