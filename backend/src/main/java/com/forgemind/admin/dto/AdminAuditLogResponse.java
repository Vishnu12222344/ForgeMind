package com.forgemind.admin.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record AdminAuditLogResponse(
        UUID id,
        UUID userId,
        String userEmail,
        String action,
        String details,
        String ipAddress,
        Instant timestamp
) {}