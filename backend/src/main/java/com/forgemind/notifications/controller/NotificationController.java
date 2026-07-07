package com.forgemind.notifications.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.notifications.dto.NotificationResponse;
import com.forgemind.notifications.dto.UnreadCountResponse;
import com.forgemind.notifications.service.NotificationService;
import com.forgemind.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(notificationService.getMyNotifications(principal.getId(), pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(notificationService.getUnreadCount(principal.getId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID notificationId) {
        return ApiResponse.success("Notification marked as read", notificationService.markAsRead(principal.getId(), notificationId));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ApiResponse.success("All notifications marked as read", null);
    }

    @DeleteMapping
    public ApiResponse<Void> clearAllNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.clearAll(principal.getId());
        return ApiResponse.success("All notifications cleared", null);
    }
}