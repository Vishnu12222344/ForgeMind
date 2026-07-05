package com.forgemind.repositories.service;

import com.forgemind.common.exception.BadRequestException;
import com.forgemind.repositories.dto.GitHubDownloadedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubRepositoryImportService {

    private final RestClient.Builder restClientBuilder;

    public GitHubDownloadedRepository downloadRepository(String repoUrl, String requestedBranch) {
        GitHubRepoPath path = parseGithubUrl(repoUrl);

        // Branches to try, in order.
        List<String> branchesToTry;

        if (requestedBranch != null && !requestedBranch.isBlank()) {
            branchesToTry = List.of(requestedBranch.trim(), "main", "master");
        } else {
            branchesToTry = List.of("main", "master");
        }

        RestClient client = restClientBuilder
                .defaultHeader("User-Agent", "ForgeMindAI")
                .defaultHeader("Accept", "application/zip")
                .build();

        for (String branch : branchesToTry) {
            byte[] zipBytes = tryDownloadBranch(client, path, branch);

            if (zipBytes != null && zipBytes.length > 0) {
                return new GitHubDownloadedRepository(
                        path.owner(),
                        path.repo(),
                        path.owner() + "/" + path.repo(),
                        "Project imported from GitHub: " + path.owner() + "/" + path.repo(),
                        branch,
                        "https://github.com/" + path.owner() + "/" + path.repo(),
                        zipBytes
                );
            }
        }

        throw new BadRequestException(
                "Could not download repository. Make sure it is public and the branch exists. Tried branches: " + branchesToTry
        );
    }

    private byte[] tryDownloadBranch(RestClient client, GitHubRepoPath path, String branch) {
        // codeload direct ZIP download URL
        String downloadUrl = "https://codeload.github.com/"
                + path.owner()
                + "/"
                + path.repo()
                + "/zip/refs/heads/"
                + branch;

        try {
            byte[] zipBytes = client
                    .get()
                    .uri(URI.create(downloadUrl))
                    .retrieve()
                    .body(byte[].class);

            if (zipBytes != null && zipBytes.length > 0) {
                log.info("Downloaded GitHub repo {}/{} branch {} ({} bytes)",
                        path.owner(), path.repo(), branch, zipBytes.length);
                return zipBytes;
            }

            return null;

        } catch (Exception ex) {
            log.warn("Failed to download branch {} for {}/{}: {}",
                    branch, path.owner(), path.repo(), ex.getMessage());
            return null;
        }
    }

    private GitHubRepoPath parseGithubUrl(String repoUrl) {
        try {
            String cleaned = repoUrl.trim();

            // Allow "owner/repo" shorthand
            if (!cleaned.contains("github.com") && cleaned.split("/").length == 2) {
                String[] shorthand = cleaned.split("/");
                return new GitHubRepoPath(shorthand[0], shorthand[1].replaceAll("\\.git$", ""));
            }

            URI uri = URI.create(cleaned);
            String host = uri.getHost();

            if (host == null || !host.equalsIgnoreCase("github.com")) {
                throw new BadRequestException("Only github.com repository URLs are supported");
            }

            String p = uri.getPath();

            if (p == null || p.isBlank()) {
                throw new BadRequestException("Invalid GitHub repository URL");
            }

            p = p.replaceAll("^/+", "").replaceAll("\\.git$", "");

            String[] parts = p.split("/");

            if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new BadRequestException("Invalid GitHub repository URL");
            }

            return new GitHubRepoPath(parts[0], parts[1]);

        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid GitHub repository URL: " + ex.getMessage());
        }
    }

    private record GitHubRepoPath(
            String owner,
            String repo
    ) {
    }
}