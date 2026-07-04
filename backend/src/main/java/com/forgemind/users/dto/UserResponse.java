package com.forgemind.users.dto;

import com.forgemind.users.entity.Role;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String email,
        String username,
        String fullName,
        String avatarUrl,
        String bio,
        Role role,
        boolean emailVerified,
        Instant createdAt
) {}