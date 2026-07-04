package com.forgemind.ai.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.ai.dto.AIConversationResponse;
import com.forgemind.ai.dto.AIConversationSummaryResponse;
import com.forgemind.ai.dto.AIMessageResponse;
import com.forgemind.ai.dto.ReferencedFileResponse;
import com.forgemind.ai.entity.AIConversation;
import com.forgemind.ai.entity.AIMessage;

import java.util.List;

public class AIMapper {

    private AIMapper() {
    }

    public static AIConversationSummaryResponse toSummary(AIConversation conversation) {
        return AIConversationSummaryResponse.builder()
                .id(conversation.getId())
                .projectId(conversation.getProject().getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public static AIConversationResponse toConversation(
            AIConversation conversation,
            List<AIMessage> messages,
            ObjectMapper objectMapper
    ) {
        return AIConversationResponse.builder()
                .id(conversation.getId())
                .projectId(conversation.getProject().getId())
                .title(conversation.getTitle())
                .messages(messages.stream().map(message -> toMessage(message, objectMapper)).toList())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public static AIMessageResponse toMessage(AIMessage message, ObjectMapper objectMapper) {
        return AIMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .promptTokens(message.getPromptTokens())
                .completionTokens(message.getCompletionTokens())
                .totalTokens(message.getTotalTokens())
                .referencedFiles(parseReferencedFiles(message.getReferencedFilesJson(), objectMapper))
                .createdAt(message.getCreatedAt())
                .build();
    }

    private static List<ReferencedFileResponse> parseReferencedFiles(
            String json,
            ObjectMapper objectMapper
    ) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }
}