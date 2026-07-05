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
        havingValue = "gemini"
)
public class GeminiAIProvider implements AIProvider {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Override
    public AIProviderResponse generate(AIProviderRequest request) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("PASTE_YOUR")) {
            throw new BadRequestException("Gemini API key is not configured");
        }

        List<AIContextFile> contextFiles = request.contextFiles() == null
                ? List.of()
                : request.contextFiles();

        String prompt = buildPrompt(request, contextFiles);

        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        String endpoint = "/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", 4096
                )
        );

        try {
            GeminiGenerateContentResponse response = restClient
                    .post()
                    .uri(endpoint)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            String content = extractText(response);

            long promptTokens = estimateTokens(prompt);
            long completionTokens = estimateTokens(content);

            return AIProviderResponse.builder()
                    .content(content)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .referencedFiles(contextFiles.stream().limit(5).toList())
                    .build();

        } catch (Exception ex) {
            throw new BadRequestException("Gemini API request failed: " + ex.getMessage());
        }
    }

    private String buildPrompt(AIProviderRequest request, List<AIContextFile> contextFiles) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            You are ForgeMind AI, an expert software engineering assistant.

            Your job:
            - Explain code clearly
            - Answer project architecture questions
            - Suggest improvements
            - Help debug issues
            - Generate useful developer documentation
            - Be concise but helpful
            - Use Markdown formatting
            - Use code blocks when showing code

            Project:
            """);

        prompt.append(request.projectName()).append("\n\n");

        prompt.append("User question:\n");
        prompt.append(request.userMessage()).append("\n\n");

        if (!contextFiles.isEmpty()) {
            prompt.append("Repository context files:\n\n");

            for (AIContextFile file : contextFiles.stream().limit(8).toList()) {
                prompt.append("File: ").append(file.path()).append("\n");
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
            prompt.append("No repository file context was provided. Answer based on the project name and user question.\n\n");
        }

        prompt.append("""
            Response requirements:
            - Answer directly.
            - If repository files were provided, reference relevant file paths.
            - If unsure, say what information is missing.
            - Keep the response practical for a developer.
            """);

        return prompt.toString();
    }

    private String extractText(GeminiGenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return "No response was returned by Gemini.";
        }

        GeminiCandidate candidate = response.candidates().get(0);

        if (candidate.content() == null ||
                candidate.content().parts() == null ||
                candidate.content().parts().isEmpty()) {
            return "Gemini returned an empty response.";
        }

        StringBuilder text = new StringBuilder();

        for (GeminiPart part : candidate.content().parts()) {
            if (part.text() != null) {
                text.append(part.text());
            }
        }

        String result = text.toString();

        return result.isBlank()
                ? "Gemini returned an empty response."
                : result;
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
            default -> "";
        };
    }

    public record GeminiGenerateContentResponse(
            List<GeminiCandidate> candidates
    ) {
    }

    public record GeminiCandidate(
            GeminiContent content
    ) {
    }

    public record GeminiContent(
            List<GeminiPart> parts,
            String role
    ) {
    }

    public record GeminiPart(
            String text
    ) {
    }
}