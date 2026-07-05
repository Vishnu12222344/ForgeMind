package com.forgemind.repositories.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.repositories.dto.GitHubRepositoryImportRequest;
import com.forgemind.repositories.dto.RepositoryImportResponse;
import com.forgemind.repositories.service.RepositoryService;
import com.forgemind.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
public class GlobalRepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<RepositoryImportResponse> uploadRepositoryWithoutProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String projectName
    ) {
        return ApiResponse.success(
                "Project created and repository uploaded successfully",
                repositoryService.uploadRepositoryWithoutProject(
                        principal.getId(),
                        file,
                        projectName
                )
        );
    }

    @PostMapping("/import/github")
    public ApiResponse<RepositoryImportResponse> importGitHubRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GitHubRepositoryImportRequest request
    ) {
        return ApiResponse.success(
                "GitHub repository imported successfully",
                repositoryService.importGitHubRepository(
                        principal.getId(),
                        request.repoUrl(),
                        request.branch()
                )
        );
    }
}