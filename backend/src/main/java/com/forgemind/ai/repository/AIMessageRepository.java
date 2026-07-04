package com.forgemind.ai.repository;

import com.forgemind.ai.entity.AIMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AIMessageRepository extends JpaRepository<AIMessage, UUID> {

    List<AIMessage> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);

    long countByConversation_Id(UUID conversationId);
}