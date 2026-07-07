package com.forgemind.notifications.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record NotificationResponse(
        UUID id,
        String title,
        String message,
        boolean read,
        String actionUrl,
        Instant createdAt
) {}