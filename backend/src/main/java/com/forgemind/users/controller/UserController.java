package com.forgemind.users.controller;

import com.forgemind.common.response.ApiResponse;
import com.forgemind.security.UserPrincipal;
import com.forgemind.users.dto.ChangePasswordRequest;
import com.forgemind.users.dto.UpdateProfileRequest;
import com.forgemind.users.dto.UserResponse;
import com.forgemind.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(userService.getById(principal.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated", userService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/me/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ApiResponse.success("Password changed successfully", null);
    }
}