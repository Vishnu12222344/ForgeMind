package com.forgemind.workspaces.repository;

import com.forgemind.workspaces.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByOwnerIdAndPersonalTrue(UUID ownerId);
    boolean existsBySlug(String slug);
}