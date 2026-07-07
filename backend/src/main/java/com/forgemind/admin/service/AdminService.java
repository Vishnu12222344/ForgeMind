package com.forgemind.admin.service;

import com.forgemind.admin.dto.AdminAuditLogResponse;
import com.forgemind.admin.dto.AdminStatsResponse;
import com.forgemind.admin.repository.AuditLogRepository;
import com.forgemind.ai.repository.AIConversationRepository;
import com.forgemind.ai.repository.AIMessageRepository;
import com.forgemind.projects.repository.ProjectRepository;
import com.forgemind.repositories.repository.RepositoryFileRepository;
import com.forgemind.repositories.repository.SourceRepositoryRepository;
import com.forgemind.users.dto.UserResponse;
import com.forgemind.users.entity.User;
import com.forgemind.users.mapper.UserMapper;
import com.forgemind.users.repository.UserRepository;
import com.forgemind.workspaces.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final SourceRepositoryRepository sourceRepositoryRepository;
    private final RepositoryFileRepository repositoryFileRepository;
    private final AIConversationRepository aiConversationRepository;
    private final AIMessageRepository aiMessageRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse getSystemStats() {
        long totalUsers = userRepository.count();
        long unverifiedUsers = userRepository.findAll().stream().filter(u -> !u.isEmailVerified()).count();
        long totalWorkspaces = workspaceRepository.count();
        long totalProjects = projectRepository.count();
        long totalRepositories = sourceRepositoryRepository.count();
        long totalFiles = repositoryFileRepository.count();
        long totalStorage = sourceRepositoryRepository.findAll().stream()
                .mapToLong(r -> r.getTotalSizeBytes()).sum();
        long totalConversations = aiConversationRepository.count();
        long totalMessages = aiMessageRepository.count();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .unverifiedUsers(unverifiedUsers)
                .totalWorkspaces(totalWorkspaces)
                .totalProjects(totalProjects)
                .totalRepositories(totalRepositories)
                .totalRepositoryFiles(totalFiles)
                .totalStorageBytes(totalStorage)
                .totalAiConversations(totalConversations)
                .totalAiMessages(totalMessages)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Transactional
    public void toggleUserStatus(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(log -> AdminAuditLogResponse.builder()
                        .id(log.getId())
                        .userId(log.getUserId())
                        .userEmail(log.getUserEmail())
                        .action(log.getAction())
                        .details(log.getDetails())
                        .ipAddress(log.getIpAddress())
                        .timestamp(log.getTimestamp())
                        .build());
    }
}