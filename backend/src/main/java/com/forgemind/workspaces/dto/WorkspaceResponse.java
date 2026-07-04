package com.forgemind.workspaces.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WorkspaceResponse(
        UUID id,
        String name,
        String slug,
        boolean personal,
        Instant createdAt
) {}