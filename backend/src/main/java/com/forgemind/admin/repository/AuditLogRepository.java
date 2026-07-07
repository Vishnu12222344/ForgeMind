package com.forgemind.admin.repository;

import com.forgemind.admin.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, java.util.UUID> {
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}