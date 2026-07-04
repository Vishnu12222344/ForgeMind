package com.forgemind.repositories.repository;

import com.forgemind.repositories.entity.SourceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SourceRepositoryRepository extends JpaRepository<SourceRepository, UUID> {

    Optional<SourceRepository> findByProject_Id(UUID projectId);

    boolean existsByProject_Id(UUID projectId);

    void deleteByProject_Id(UUID projectId);
}