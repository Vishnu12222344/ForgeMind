package com.forgemind.ai.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "mock",
        matchIfMissing = true
)
public class MockAIProvider implements AIProvider {

    @Override
    public AIProviderResponse generate(AIProviderRequest request) {
        List<AIContextFile> contextFiles = request.contextFiles() == null
                ? List.of()
                : request.contextFiles();

        StringBuilder response = new StringBuilder();

        response.append("## ForgeMind AI Response\n\n");
        response.append("You asked:\n\n");
        response.append("> ").append(request.userMessage()).append("\n\n");

        response.append("Project context:\n\n");
        response.append("- Project: `").append(request.projectName()).append("`\n");
        response.append("- Context files available: ").append(contextFiles.size()).append("\n\n");

        if (!contextFiles.isEmpty()) {
            response.append("Referenced files:\n\n");

            contextFiles.stream()
                    .limit(5)
                    .forEach(file -> response
                            .append("- `")
                            .append(file.path())
                            .append("`")
                            .append(file.language() != null ? " (" + file.language() + ")" : "")
                            .append("\n")
                    );

            response.append("\n");
        }

        response.append("### Answer\n\n");
        response.append("This is a mock AI response. Set `app.ai.provider=gemini` to use real AI.\n");

        long promptTokens = estimateTokens(request.userMessage()) + contextFiles.size() * 200L;
        long completionTokens = estimateTokens(response.toString());

        return AIProviderResponse.builder()
                .content(response.toString())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .referencedFiles(contextFiles.stream().limit(5).toList())
                .build();
    }

    private long estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return Math.max(1, text.length() / 4);
    }
}