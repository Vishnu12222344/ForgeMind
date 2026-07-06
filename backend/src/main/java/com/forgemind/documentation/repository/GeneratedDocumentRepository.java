package com.forgemind.documentation.repository;

import com.forgemind.documentation.entity.DocumentationType;
import com.forgemind.documentation.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, UUID> {

    List<GeneratedDocument> findByProject_IdOrderByCreatedAtDesc(UUID projectId);

    Optional<GeneratedDocument> findByIdAndProject_Id(UUID id, UUID projectId);

    Optional<GeneratedDocument> findTopByProject_IdAndTypeOrderByCreatedAtDesc(
            UUID projectId,
            DocumentationType type
    );
}