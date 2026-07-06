package com.forgemind.users.service;

import com.forgemind.common.exception.BadRequestException;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.users.dto.ChangePasswordRequest;
import com.forgemind.users.dto.UpdateProfileRequest;
import com.forgemind.users.dto.UserResponse;
import com.forgemind.users.entity.User;
import com.forgemind.users.entity.UserAvatar;
import com.forgemind.users.mapper.UserMapper;
import com.forgemind.users.repository.UserAvatarRepository;
import com.forgemind.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAvatarRepository userAvatarRepository; // Add this line

    public UserResponse getById(UUID id) {
        return UserMapper.toResponse(findById(id));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findById(userId);
        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.bio() != null) user.setBio(request.bio());
        return UserMapper.toResponse(userRepository.save(user));
    }
    @Transactional
    public UserResponse uploadAvatar(UUID userId, MultipartFile file, String requestBaseUrl) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Invalid image file");
        }

        try {
            UserAvatar avatar = UserAvatar.builder()
                    .userId(userId)
                    .contentType(file.getContentType())
                    .data(file.getBytes())
                    .build();
            userAvatarRepository.save(avatar);

            User user = findById(userId);
            user.setAvatarUrl(requestBaseUrl + "/api/v1/users/" + userId + "/avatar");
            return UserMapper.toResponse(userRepository.save(user));
        } catch (Exception e) {
            throw new BadRequestException("Failed to upload image");
        }
    }

    @Transactional(readOnly = true)
    public UserAvatar getAvatar(UUID userId) {
        return userAvatarRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found"));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}