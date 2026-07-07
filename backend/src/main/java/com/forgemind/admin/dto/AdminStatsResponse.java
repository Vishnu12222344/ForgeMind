package com.forgemind.admin.dto;

import lombok.Builder;

@Builder
public record AdminStatsResponse(
        long totalUsers,
        long unverifiedUsers,
        long totalWorkspaces,
        long totalProjects,
        long totalRepositories,
        long totalRepositoryFiles,
        long totalStorageBytes,
        long totalAiConversations,
        long totalAiMessages
) {}