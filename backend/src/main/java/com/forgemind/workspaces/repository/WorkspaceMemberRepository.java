package com.forgemind.workspaces.repository;

import com.forgemind.workspaces.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    // Keep old-style method for existing code compatibility
    List<WorkspaceMember> findByUserId(UUID userId);

    // Safer nested property methods
    List<WorkspaceMember> findByUser_Id(UUID userId);

    boolean existsByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

    Optional<WorkspaceMember> findByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);
}