package com.forgemind.auth.dto;

public record OAuthUserInfo(
        String provider,
        String providerId,
        String email,
        String name,
        String username,
        String avatarUrl
) {
}