package com.forgemind.ai.provider;

import com.forgemind.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "glm"
)
public class GlmAIProvider implements AIProvider {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.ai.glm.api-key:}")
    private String apiKey;

    @Value("${app.ai.glm.model:glm-4.6}")
    private String model;

    @Value("${app.ai.glm.base-url:https://api.z.ai/api/paas/v4}")
    private String baseUrl;

    @Override
    public AIProviderResponse generate(AIProviderRequest request) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("PASTE_YOUR")) {
            throw new BadRequestException("GLM API key is not configured");
        }

        List<AIContextFile> contextFiles = request.contextFiles() == null
                ? List.of()
                : request.contextFiles();

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request, contextFiles);

        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 4096
        );

        try {
            GlmChatCompletionResponse response = restClient
                    .post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(GlmChatCompletionResponse.class);

            String content = extractText(response);

            long promptTokens = response != null && response.usage() != null
                    ? response.usage().prompt_tokens()
                    : estimateTokens(systemPrompt + "\n" + userPrompt);

            long completionTokens = response != null && response.usage() != null
                    ? response.usage().completion_tokens()
                    : estimateTokens(content);

            long totalTokens = response != null && response.usage() != null
                    ? response.usage().total_tokens()
                    : promptTokens + completionTokens;

            return AIProviderResponse.builder()
                    .content(content)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .referencedFiles(contextFiles.stream().limit(5).toList())
                    .build();

        } catch (Exception ex) {
            throw new BadRequestException("GLM API request failed: " + ex.getMessage());
        }
    }

    private String buildSystemPrompt() {
        return """
            You are ForgeMind AI, an expert software engineering assistant.

            Your job:
            - Explain code clearly
            - Answer software architecture questions
            - Help developers understand repositories
            - Suggest improvements
            - Help debug errors
            - Generate practical documentation
            - Use clear Markdown formatting
            - Use code blocks when needed
            - Be concise but useful
            - If repository context is provided, reference relevant file paths
            - If information is missing, say what is missing instead of hallucinating
            """;
    }

    private String buildUserPrompt(AIProviderRequest request, List<AIContextFile> contextFiles) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Project name:\n");
        prompt.append(
                request.projectName() == null || request.projectName().isBlank()
                        ? "General ForgeMind AI Assistant"
                        : request.projectName()
        ).append("\n\n");

        prompt.append("User question:\n");
        prompt.append(request.userMessage()).append("\n\n");

        if (!contextFiles.isEmpty()) {
            prompt.append("Repository context files:\n\n");

            for (AIContextFile file : contextFiles.stream().limit(8).toList()) {
                prompt.append("File path: ").append(file.path()).append("\n");
                prompt.append("Language: ").append(file.language()).append("\n");
                prompt.append("Content:\n");
                prompt.append("```").append(toMarkdownFence(file.language())).append("\n");

                String content = file.content() == null ? "" : file.content();

                if (content.length() > 6000) {
                    content = content.substring(0, 6000) + "\n// ... truncated";
                }

                prompt.append(content).append("\n");
                prompt.append("```\n\n");
            }
        } else {
            prompt.append("No repository file context was provided.\n\n");
        }

        prompt.append("""
            Response requirements:
            - Answer the user directly.
            - Use Markdown.
            - If files were provided, mention relevant file paths.
            - Give practical developer-focused guidance.
            """);

        return prompt.toString();
    }

    private String extractText(GlmChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "No response was returned by GLM.";
        }

        GlmChoice choice = response.choices().get(0);

        if (choice.message() == null || choice.message().content() == null) {
            return "GLM returned an empty response.";
        }

        String content = choice.message().content();

        return content.isBlank() ? "GLM returned an empty response." : content;
    }

    private long estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }

    private String toMarkdownFence(String language) {
        if (language == null) {
            return "";
        }

        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "javascript" -> "javascript";
            case "typescript" -> "typescript";
            case "python" -> "python";
            case "html" -> "html";
            case "css" -> "css";
            case "json" -> "json";
            case "yaml" -> "yaml";
            case "markdown" -> "markdown";
            case "sql" -> "sql";
            case "shell" -> "bash";
            case "go" -> "go";
            case "rust" -> "rust";
            case "kotlin" -> "kotlin";
            default -> "";
        };
    }

    public record GlmChatCompletionResponse(
            String id,
            String model,
            List<GlmChoice> choices,
            GlmUsage usage
    ) {
    }

    public record GlmChoice(
            Integer index,
            GlmMessage message,
            String finish_reason
    ) {
    }

    public record GlmMessage(
            String role,
            String content
    ) {
    }

    public record GlmUsage(
            long prompt_tokens,
            long completion_tokens,
            long total_tokens
    ) {
    }
}