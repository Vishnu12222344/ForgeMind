package com.forgemind.users.service;

import com.forgemind.common.exception.BadRequestException;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.users.dto.ChangePasswordRequest;
import com.forgemind.users.dto.UpdateProfileRequest;
import com.forgemind.users.dto.UserResponse;
import com.forgemind.users.entity.User;
import com.forgemind.users.mapper.UserMapper;
import com.forgemind.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}