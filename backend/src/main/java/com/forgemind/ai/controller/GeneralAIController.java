package com.forgemind.ai.controller;

import com.forgemind.ai.dto.AIConversationResponse;
import com.forgemind.ai.dto.AIConversationSummaryResponse;
import com.forgemind.ai.dto.GeneralAIChatRequest;
import com.forgemind.ai.dto.GeneralAIChatResponse;
import com.forgemind.ai.service.GeneralAIService;
import com.forgemind.common.response.ApiResponse;
import com.forgemind.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class GeneralAIController {

    private final GeneralAIService generalAIService;

    @PostMapping("/chat")
    public ApiResponse<GeneralAIChatResponse> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GeneralAIChatRequest request
    ) {
        return ApiResponse.success(
                "AI response generated successfully",
                generalAIService.chat(principal.getId(), request)
        );
    }

    @GetMapping("/conversations")
    public ApiResponse<List<AIConversationSummaryResponse>> getConversations(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(generalAIService.getConversations(principal.getId()));
    }

    @GetMapping("/conversations/{conversationId}")
    public ApiResponse<AIConversationResponse> getConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId) {
        return ApiResponse.success(generalAIService.getConversation(principal.getId(), conversationId));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Void> deleteConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId) {
        generalAIService.deleteConversation(principal.getId(), conversationId);
        return ApiResponse.success("Deleted", null);
    }
}