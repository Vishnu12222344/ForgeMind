package com.forgemind.repositories.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.repositories.dto.RepositoryFileResponse;
import com.forgemind.repositories.dto.RepositoryResponse;
import com.forgemind.repositories.dto.RepositoryTreeNodeResponse;
import com.forgemind.repositories.service.RepositoryService;
import com.forgemind.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/repository")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<RepositoryResponse> uploadRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean replaceExisting
    ) {
        return ApiResponse.success(
                "Repository uploaded and parsed successfully",
                repositoryService.uploadRepository(
                        principal.getId(),
                        projectId,
                        file,
                        replaceExisting
                )
        );
    }

    @GetMapping
    public ApiResponse<RepositoryResponse> getRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                repositoryService.getRepository(principal.getId(), projectId)
        );
    }

    @GetMapping("/tree")
    public ApiResponse<List<RepositoryTreeNodeResponse>> getRepositoryTree(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                repositoryService.getRepositoryTree(principal.getId(), projectId)
        );
    }

    @GetMapping("/file/{fileId}")
    public ApiResponse<RepositoryFileResponse> getFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID fileId
    ) {
        return ApiResponse.success(
                repositoryService.getFile(principal.getId(), projectId, fileId)
        );
    }

    @DeleteMapping
    public ApiResponse<Void> deleteRepository(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        repositoryService.deleteRepository(principal.getId(), projectId);

        return ApiResponse.success("Repository deleted successfully", null);
    }
}