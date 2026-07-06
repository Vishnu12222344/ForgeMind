package com.forgemind.ai.repository;

import com.forgemind.ai.entity.AIConversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AIConversationRepository extends JpaRepository<AIConversation, UUID> {

    List<AIConversation> findByProject_IdOrderByUpdatedAtDesc(UUID projectId);

    Optional<AIConversation> findByIdAndProject_Id(UUID id, UUID projectId);
    List<AIConversation> findByUser_IdAndProjectIsNullOrderByUpdatedAtDesc(UUID userId);

    Optional<AIConversation> findByIdAndUser_Id(UUID id, UUID userId);

    @EntityGraph(attributePaths = {"project", "user"})
    Optional<AIConversation> findWithProjectAndUserByIdAndProject_Id(UUID id, UUID projectId);
}