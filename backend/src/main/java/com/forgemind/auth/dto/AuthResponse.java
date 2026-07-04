package com.forgemind.auth.dto;

import com.forgemind.users.dto.UserResponse;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {}