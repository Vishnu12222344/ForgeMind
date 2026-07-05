package com.forgemind.repositories.dto;

public record GitHubDownloadedRepository(
        String owner,
        String repoName,
        String fullName,
        String description,
        String branch,
        String htmlUrl,
        byte[] zipBytes
) {
}