package com.forgemind.users.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 255) String fullName,
        @Size(max = 500) String avatarUrl,
        @Size(max = 1000) String bio
) {}