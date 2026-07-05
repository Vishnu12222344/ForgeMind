package com.forgemind.ai.controller;

import com.forgemind.ai.dto.GeneralAIChatRequest;
import com.forgemind.ai.dto.GeneralAIChatResponse;
import com.forgemind.ai.service.GeneralAIService;
import com.forgemind.common.response.ApiResponse;
import com.forgemind.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}