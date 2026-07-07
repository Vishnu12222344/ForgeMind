package com.forgemind.admin.controller;

import com.forgemind.admin.dto.AdminAuditLogResponse;
import com.forgemind.admin.dto.AdminStatsResponse;
import com.forgemind.admin.service.AdminService;
import com.forgemind.common.response.ApiResponse;
import com.forgemind.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> getSystemStats() {
        return ApiResponse.success(adminService.getSystemStats());
    }

    @GetMapping("/users")
    public ApiResponse<Page<UserResponse>> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(adminService.listUsers(pageable));
    }

    @PatchMapping("/users/{userId}/toggle")
    public ApiResponse<Void> toggleUserStatus(@PathVariable UUID userId, @RequestParam boolean enabled) {
        adminService.toggleUserStatus(userId, enabled);
        return ApiResponse.success("User account status updated", null);
    }

    @GetMapping("/audit-logs")
    public ApiResponse<Page<AdminAuditLogResponse>> getAuditLogs(@PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.success(adminService.getAuditLogs(pageable));
    }
}