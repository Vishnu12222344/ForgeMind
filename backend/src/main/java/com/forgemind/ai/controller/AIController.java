package com.forgemind.ai.controller;

import com.forgemind.ai.dto.AIChatRequest;
import com.forgemind.ai.dto.AIChatResponse;
import com.forgemind.ai.dto.AIConversationResponse;
import com.forgemind.ai.dto.AIConversationSummaryResponse;
import com.forgemind.ai.service.AIChatService;
import com.forgemind.common.response.ApiResponse;
import com.forgemind.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIChatService aiChatService;

    @PostMapping("/chat")
    public ApiResponse<AIChatResponse> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody AIChatRequest request
    ) {
        return ApiResponse.success(
                "AI response generated successfully",
                aiChatService.chat(principal.getId(), projectId, request)
        );
    }

    @GetMapping("/conversations")
    public ApiResponse<List<AIConversationSummaryResponse>> getConversations(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(
                aiChatService.getConversations(principal.getId(), projectId)
        );
    }

    @GetMapping("/conversations/{conversationId}")
    public ApiResponse<AIConversationResponse> getConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID conversationId
    ) {
        return ApiResponse.success(
                aiChatService.getConversation(principal.getId(), projectId, conversationId)
        );
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Void> deleteConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID conversationId
    ) {
        aiChatService.deleteConversation(principal.getId(), projectId, conversationId);

        return ApiResponse.success("AI conversation deleted successfully", null);
    }
}