package com.forgemind.ai.service;

import com.forgemind.ai.dto.GeneralAIChatRequest;
import com.forgemind.ai.dto.GeneralAIChatResponse;
import com.forgemind.ai.provider.AIProvider;
import com.forgemind.ai.provider.AIProviderRequest;
import com.forgemind.ai.provider.AIProviderResponse;
import com.forgemind.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeneralAIService {

    private final AIProvider aiProvider;
    private final UserService userService;

    public GeneralAIChatResponse chat(UUID userId, GeneralAIChatRequest request) {
        userService.findById(userId);

        AIProviderResponse response = aiProvider.generate(
                AIProviderRequest.builder()
                        .projectId(null)
                        .projectName("General ForgeMind AI Assistant")
                        .userMessage(request.message())
                        .contextFiles(List.of())
                        .build()
        );

        return GeneralAIChatResponse.builder()
                .content(response.content())
                .promptTokens(response.promptTokens())
                .completionTokens(response.completionTokens())
                .totalTokens(response.totalTokens())
                .build();
    }
}