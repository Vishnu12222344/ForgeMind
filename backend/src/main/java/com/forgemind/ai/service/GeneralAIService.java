package com.forgemind.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.ai.dto.*;
import com.forgemind.ai.entity.AIConversation;
import com.forgemind.ai.entity.AIMessage;
import com.forgemind.ai.entity.AIMessageRole;
import com.forgemind.ai.mapper.AIMapper;
import com.forgemind.ai.provider.AIProvider;
import com.forgemind.ai.provider.AIProviderRequest;
import com.forgemind.ai.provider.AIProviderResponse;
import com.forgemind.ai.repository.AIConversationRepository;
import com.forgemind.ai.repository.AIMessageRepository;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.users.entity.User;
import com.forgemind.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeneralAIService {

    private final AIProvider aiProvider;
    private final UserService userService;
    private final AIConversationRepository conversationRepository;
    private final AIMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public GeneralAIChatResponse chat(UUID userId, GeneralAIChatRequest request) {
        User user = userService.findById(userId);

        AIConversation conversation = getOrCreateConversation(user, request);

        AIMessage userMessage = AIMessage.builder()
                .conversation(conversation)
                .role(AIMessageRole.USER)
                .content(request.message().trim())
                .build();

        userMessage = messageRepository.save(userMessage);

        AIProviderResponse providerResponse = aiProvider.generate(
                AIProviderRequest.builder()
                        .projectId(null)
                        .projectName("General ForgeMind AI Assistant")
                        .userMessage(request.message())
                        .contextFiles(List.of())
                        .build()
        );

        AIMessage assistantMessage = AIMessage.builder()
                .conversation(conversation)
                .role(AIMessageRole.ASSISTANT)
                .content(providerResponse.content())
                .promptTokens(providerResponse.promptTokens())
                .completionTokens(providerResponse.completionTokens())
                .totalTokens(providerResponse.totalTokens())
                .build();

        assistantMessage = messageRepository.save(assistantMessage);

        return GeneralAIChatResponse.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .userMessage(AIMapper.toMessage(userMessage, objectMapper))
                .assistantMessage(AIMapper.toMessage(assistantMessage, objectMapper))
                .build();
    }

    @Transactional(readOnly = true)
    public List<AIConversationSummaryResponse> getConversations(UUID userId) {
        return conversationRepository.findByUser_IdAndProjectIsNullOrderByUpdatedAtDesc(userId)
                .stream()
                .map(AIMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AIConversationResponse getConversation(UUID userId, UUID conversationId) {
        AIConversation conversation = conversationRepository.findByIdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        List<AIMessage> messages = messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId());

        return AIMapper.toConversation(conversation, messages, objectMapper);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID conversationId) {
        AIConversation conversation = conversationRepository.findByIdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        conversationRepository.delete(conversation);
    }

    private AIConversation getOrCreateConversation(User user, GeneralAIChatRequest request) {
        if (request.conversationId() != null) {
            return conversationRepository.findByIdAndUser_Id(request.conversationId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        }
        String title = request.message().trim();
        if (title.length() > 60) title = title.substring(0, 60) + "...";

        return conversationRepository.save(AIConversation.builder()
                .user(user)
                .project(null) // No project
                .title(title)
                .build());
    }
}