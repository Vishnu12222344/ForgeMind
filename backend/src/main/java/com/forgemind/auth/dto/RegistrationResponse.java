package com.forgemind.auth.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RegistrationResponse(
        UUID userId,
        String email,
        boolean emailVerificationRequired
) {
}