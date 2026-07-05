package com.forgemind.repositories.service;

import com.forgemind.common.exception.BadRequestException;
import com.forgemind.repositories.dto.GitHubDownloadedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryImportService {

    private final RestClient.Builder restClientBuilder;

    public GitHubDownloadedRepository downloadRepository(String repoUrl, String requestedBranch) {
        GitHubRepoPath path = parseGithubUrl(repoUrl);

        RestClient githubClient = restClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "ForgeMindAI")
                .build();

        Map<?, ?> repoMetadata;

        try {
            repoMetadata = githubClient
                    .get()
                    .uri("/repos/{owner}/{repo}", path.owner(), path.repo())
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to access GitHub repository. Make sure the repository is public.");
        }

        if (repoMetadata == null) {
            throw new BadRequestException("GitHub repository not found");
        }

        String defaultBranch = stringValue(repoMetadata.get("default_branch"));
        String description = stringValue(repoMetadata.get("description"));
        String htmlUrl = stringValue(repoMetadata.get("html_url"));
        String fullName = stringValue(repoMetadata.get("full_name"));

        String branch = requestedBranch != null && !requestedBranch.isBlank()
                ? requestedBranch.trim()
                : defaultBranch;

        if (branch == null || branch.isBlank()) {
            branch = "main";
        }

        String downloadUrl = "https://codeload.github.com/"
                + path.owner()
                + "/"
                + path.repo()
                + "/zip/refs/heads/"
                + branch;

        byte[] zipBytes;

        try {
            zipBytes = restClientBuilder
                    .defaultHeader("User-Agent", "ForgeMindAI")
                    .build()
                    .get()
                    .uri(downloadUrl)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception ex) {
            throw new BadRequestException("Failed to download GitHub repository ZIP. Check branch name.");
        }

        if (zipBytes == null || zipBytes.length == 0) {
            throw new BadRequestException("Downloaded GitHub repository ZIP is empty");
        }

        return new GitHubDownloadedRepository(
                path.owner(),
                path.repo(),
                fullName == null ? path.owner() + "/" + path.repo() : fullName,
                description,
                branch,
                htmlUrl,
                zipBytes
        );
    }

    private GitHubRepoPath parseGithubUrl(String repoUrl) {
        try {
            URI uri = URI.create(repoUrl.trim());

            String host = uri.getHost();

            if (host == null || !host.equalsIgnoreCase("github.com")) {
                throw new BadRequestException("Only github.com repository URLs are supported");
            }

            String path = uri.getPath();

            if (path == null || path.isBlank()) {
                throw new BadRequestException("Invalid GitHub repository URL");
            }

            path = path.replaceAll("^/+", "").replaceAll("\\.git$", "");

            String[] parts = path.split("/");

            if (parts.length < 2) {
                throw new BadRequestException("Invalid GitHub repository URL");
            }

            return new GitHubRepoPath(parts[0], parts[1]);

        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid GitHub repository URL");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record GitHubRepoPath(
            String owner,
            String repo
    ) {
    }
}