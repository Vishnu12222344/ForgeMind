package com.forgemind.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgemind.ai.dto.AIChatRequest;
import com.forgemind.ai.dto.AIChatResponse;
import com.forgemind.ai.dto.AIConversationResponse;
import com.forgemind.ai.dto.AIConversationSummaryResponse;
import com.forgemind.ai.dto.ReferencedFileResponse;
import com.forgemind.ai.entity.AIConversation;
import com.forgemind.ai.entity.AIMessage;
import com.forgemind.ai.entity.AIMessageRole;
import com.forgemind.ai.mapper.AIMapper;
import com.forgemind.ai.provider.AIContextFile;
import com.forgemind.ai.provider.AIProvider;
import com.forgemind.ai.provider.AIProviderRequest;
import com.forgemind.ai.provider.AIProviderResponse;
import com.forgemind.ai.repository.AIConversationRepository;
import com.forgemind.ai.repository.AIMessageRepository;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.projects.entity.Project;
import com.forgemind.projects.service.ProjectService;
import com.forgemind.users.entity.User;
import com.forgemind.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final AIConversationRepository conversationRepository;
    private final AIMessageRepository messageRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final AIContextService aiContextService;
    private final AIProvider aiProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public AIChatResponse chat(UUID userId, UUID projectId, AIChatRequest request) {
        Project project = projectService.getProjectEntityForUser(userId, projectId);
        User user = userService.findById(userId);

        AIConversation conversation = getOrCreateConversation(project, user, request);

        AIMessage userMessage = AIMessage.builder()
                .conversation(conversation)
                .role(AIMessageRole.USER)
                .content(request.message().trim())
                .promptTokens(0)
                .completionTokens(0)
                .totalTokens(0)
                .referencedFilesJson(null)
                .build();

        userMessage = messageRepository.save(userMessage);

        List<AIContextFile> contextFiles = aiContextService.collectContextFiles(
                projectId,
                request.fileIds()
        );

        AIProviderResponse providerResponse = aiProvider.generate(
                AIProviderRequest.builder()
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .userMessage(request.message())
                        .contextFiles(contextFiles)
                        .build()
        );

        AIMessage assistantMessage = AIMessage.builder()
                .conversation(conversation)
                .role(AIMessageRole.ASSISTANT)
                .content(providerResponse.content())
                .promptTokens(providerResponse.promptTokens())
                .completionTokens(providerResponse.completionTokens())
                .totalTokens(providerResponse.totalTokens())
                .referencedFilesJson(toReferencedFilesJson(providerResponse.referencedFiles()))
                .build();

        assistantMessage = messageRepository.save(assistantMessage);

        return AIChatResponse.builder()
                .conversationId(conversation.getId())
                .title(conversation.getTitle())
                .userMessage(AIMapper.toMessage(userMessage, objectMapper))
                .assistantMessage(AIMapper.toMessage(assistantMessage, objectMapper))
                .build();
    }

    @Transactional(readOnly = true)
    public List<AIConversationSummaryResponse> getConversations(UUID userId, UUID projectId) {
        projectService.getProjectEntityForUser(userId, projectId);

        return conversationRepository.findByProject_IdOrderByUpdatedAtDesc(projectId)
                .stream()
                .map(AIMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AIConversationResponse getConversation(UUID userId, UUID projectId, UUID conversationId) {
        projectService.getProjectEntityForUser(userId, projectId);

        AIConversation conversation = conversationRepository.findWithProjectAndUserByIdAndProject_Id(
                        conversationId,
                        projectId
                )
                .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found"));

        List<AIMessage> messages = messageRepository.findByConversation_IdOrderByCreatedAtAsc(
                conversation.getId()
        );

        return AIMapper.toConversation(conversation, messages, objectMapper);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID projectId, UUID conversationId) {
        projectService.getProjectEntityForUser(userId, projectId);

        AIConversation conversation = conversationRepository.findByIdAndProject_Id(
                        conversationId,
                        projectId
                )
                .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found"));

        conversationRepository.delete(conversation);
    }

    private AIConversation getOrCreateConversation(
            Project project,
            User user,
            AIChatRequest request
    ) {
        if (request.conversationId() != null) {
            return conversationRepository.findByIdAndProject_Id(
                            request.conversationId(),
                            project.getId()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("AI conversation not found"));
        }

        String title = generateTitle(request.message());

        AIConversation conversation = AIConversation.builder()
                .project(project)
                .user(user)
                .title(title)
                .build();

        return conversationRepository.save(conversation);
    }

    private String generateTitle(String message) {
        if (message == null || message.isBlank()) {
            return "New conversation";
        }

        String cleaned = message.trim().replaceAll("\\s+", " ");

        if (cleaned.length() <= 60) {
            return cleaned;
        }

        return cleaned.substring(0, 60) + "...";
    }

    private String toReferencedFilesJson(List<AIContextFile> files) {
        if (files == null || files.isEmpty()) {
            return "[]";
        }

        try {
            List<ReferencedFileResponse> references = files.stream()
                    .map(file -> ReferencedFileResponse.builder()
                            .id(file.id())
                            .path(file.path())
                            .name(file.name())
                            .language(file.language())
                            .build()
                    )
                    .toList();

            return objectMapper.writeValueAsString(references);
        } catch (Exception ex) {
            return "[]";
        }
    }
}