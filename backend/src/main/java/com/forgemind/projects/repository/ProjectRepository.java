package com.forgemind.projects.repository;

import com.forgemind.projects.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    boolean existsByWorkspace_IdAndSlug(UUID workspaceId, String slug);

    Optional<Project> findByWorkspace_IdAndSlug(UUID workspaceId, String slug);

    @EntityGraph(attributePaths = {"workspace", "createdBy", "tags"})
    @Query("select p from Project p where p.id = :id")
    Optional<Project> findByIdWithRelations(@Param("id") UUID id);
}