package com.forgemind.projects.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.projects.dto.CreateProjectRequest;
import com.forgemind.projects.dto.ProjectResponse;
import com.forgemind.projects.dto.UpdateProjectRequest;
import com.forgemind.projects.entity.ProjectVisibility;
import com.forgemind.projects.service.ProjectService;
import com.forgemind.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<Page<ProjectResponse>> getProjects(
            @AuthenticationPrincipal UserPrincipal principal,

            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) ProjectVisibility visibility,
            @RequestParam(required = false) String tag,

            @PageableDefault(
                    size = 20,
                    sort = "updatedAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ApiResponse.success(projectService.getProjects(
                principal.getId(),
                search,
                archived,
                favorite,
                visibility,
                tag,
                pageable
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> createProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return ApiResponse.success(
                "Project created successfully",
                projectService.createProject(principal.getId(), request)
        );
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> getProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(projectService.getProject(principal.getId(), projectId));
    }

    @PutMapping("/{projectId}")
    public ApiResponse<ProjectResponse> updateProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ApiResponse.success(
                "Project updated successfully",
                projectService.updateProject(principal.getId(), projectId, request)
        );
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> deleteProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        projectService.deleteProject(principal.getId(), projectId);
        return ApiResponse.success("Project deleted successfully", null);
    }

    @PatchMapping("/{projectId}/archive")
    public ApiResponse<ProjectResponse> archiveProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                "Project archived successfully",
                projectService.archiveProject(principal.getId(), projectId)
        );
    }

    @PatchMapping("/{projectId}/unarchive")
    public ApiResponse<ProjectResponse> unarchiveProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                "Project unarchived successfully",
                projectService.unarchiveProject(principal.getId(), projectId)
        );
    }

    @PatchMapping("/{projectId}/favorite")
    public ApiResponse<ProjectResponse> favoriteProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                "Project added to favorites",
                projectService.favoriteProject(principal.getId(), projectId)
        );
    }

    @PatchMapping("/{projectId}/unfavorite")
    public ApiResponse<ProjectResponse> unfavoriteProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                "Project removed from favorites",
                projectService.unfavoriteProject(principal.getId(), projectId)
        );
    }
}