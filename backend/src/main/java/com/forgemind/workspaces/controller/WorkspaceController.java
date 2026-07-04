package com.forgemind.workspaces.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.security.UserPrincipal;
import com.forgemind.workspaces.dto.WorkspaceResponse;
import com.forgemind.workspaces.entity.Workspace;
import com.forgemind.workspaces.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/me")
    public ApiResponse<WorkspaceResponse> getMyWorkspace(@AuthenticationPrincipal UserPrincipal principal) {
        Workspace workspace = workspaceService.getPersonalWorkspace(principal.getId());
        return ApiResponse.success(WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .personal(workspace.isPersonal())
                .createdAt(workspace.getCreatedAt())
                .build());
    }
}