package com.forgemind.documentation.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.documentation.dto.GenerateDocumentationRequest;
import com.forgemind.documentation.dto.GeneratedDocumentResponse;
import com.forgemind.documentation.dto.GeneratedDocumentSummaryResponse;
import com.forgemind.documentation.entity.DocumentationType;
import com.forgemind.documentation.service.DocumentationService;
import com.forgemind.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/documentation")
@RequiredArgsConstructor
public class DocumentationController {

    private final DocumentationService documentationService;

    @PostMapping("/generate")
    public ApiResponse<GeneratedDocumentResponse> generate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody GenerateDocumentationRequest request
    ) {
        return ApiResponse.success(
                "Documentation generated successfully",
                documentationService.generate(principal.getId(), projectId, request)
        );
    }

    @GetMapping
    public ApiResponse<List<GeneratedDocumentSummaryResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                documentationService.list(principal.getId(), projectId)
        );
    }

    @GetMapping("/{documentId}")
    public ApiResponse<GeneratedDocumentResponse> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID documentId
    ) {
        return ApiResponse.success(
                documentationService.get(principal.getId(), projectId, documentId)
        );
    }

    @GetMapping("/latest")
    public ApiResponse<GeneratedDocumentResponse> latest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @RequestParam DocumentationType type
    ) {
        return ApiResponse.success(
                documentationService.latest(principal.getId(), projectId, type)
        );
    }

    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID documentId
    ) {
        documentationService.delete(principal.getId(), projectId, documentId);
        return ApiResponse.success("Document deleted successfully", null);
    }
}