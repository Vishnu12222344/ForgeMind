package com.forgemind.admin.service;

import com.forgemind.admin.entity.AuditLog;
import com.forgemind.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UUID userId, String email, String action, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .userEmail(email)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}