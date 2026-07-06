package com.forgemind.documentation.service;

import com.forgemind.ai.provider.AIProvider;
import com.forgemind.ai.provider.AIProviderRequest;
import com.forgemind.ai.provider.AIProviderResponse;
import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.documentation.dto.GenerateDocumentationRequest;
import com.forgemind.documentation.dto.GeneratedDocumentResponse;
import com.forgemind.documentation.dto.GeneratedDocumentSummaryResponse;
import com.forgemind.documentation.entity.DocumentationType;
import com.forgemind.documentation.entity.GeneratedDocument;
import com.forgemind.documentation.mapper.DocumentationMapper;
import com.forgemind.documentation.repository.GeneratedDocumentRepository;
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
public class DocumentationService {

    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final DocumentationContextService documentationContextService;
    private final AIProvider aiProvider;

    @Transactional
    public GeneratedDocumentResponse generate(
            UUID userId,
            UUID projectId,
            GenerateDocumentationRequest request
    ) {
        Project project = projectService.getProjectEntityForUser(userId, projectId);
        User user = userService.findById(userId);

        DocumentationType type = request.type() == null
                ? DocumentationType.FULL_DOCUMENTATION
                : request.type();

        boolean includeFlowcharts = request.includeFlowcharts() == null
                || request.includeFlowcharts();

        DocumentationContext context = documentationContextService.buildContext(project);

        String prompt = buildPrompt(project, type, includeFlowcharts, context, request.additionalInstructions());

        AIProviderResponse aiResponse = aiProvider.generate(
                AIProviderRequest.builder()
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .userMessage(prompt)
                        .contextFiles(context.contextFiles())
                        .build()
        );

        GeneratedDocument document = GeneratedDocument.builder()
                .project(project)
                .generatedBy(user)
                .type(type)
                .title(buildTitle(project.getName(), type))
                .content(aiResponse.content())
                .promptTokens(aiResponse.promptTokens())
                .completionTokens(aiResponse.completionTokens())
                .totalTokens(aiResponse.totalTokens())
                .build();

        GeneratedDocument saved = generatedDocumentRepository.save(document);

        return DocumentationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GeneratedDocumentSummaryResponse> list(UUID userId, UUID projectId) {
        projectService.getProjectEntityForUser(userId, projectId);

        return generatedDocumentRepository.findByProject_IdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(DocumentationMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse get(UUID userId, UUID projectId, UUID documentId) {
        projectService.getProjectEntityForUser(userId, projectId);

        GeneratedDocument document = generatedDocumentRepository
                .findByIdAndProject_Id(documentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        return DocumentationMapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public GeneratedDocumentResponse latest(UUID userId, UUID projectId, DocumentationType type) {
        projectService.getProjectEntityForUser(userId, projectId);

        GeneratedDocument document = generatedDocumentRepository
                .findTopByProject_IdAndTypeOrderByCreatedAtDesc(projectId, type)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        return DocumentationMapper.toResponse(document);
    }

    @Transactional
    public void delete(UUID userId, UUID projectId, UUID documentId) {
        projectService.getProjectEntityForUser(userId, projectId);

        GeneratedDocument document = generatedDocumentRepository
                .findByIdAndProject_Id(documentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        generatedDocumentRepository.delete(document);
    }

    private String buildPrompt(
            Project project,
            DocumentationType type,
            boolean includeFlowcharts,
            DocumentationContext context,
            String additionalInstructions
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            Generate professional software project documentation for the following project.

            You are ForgeMind AI, a senior software architect and technical documentation expert.

            Output must be in clean Markdown.

            Project metadata:
            """);

        prompt.append("- Project name: ").append(project.getName()).append("\n");
        prompt.append("- Description: ").append(project.getDescription() == null ? "Not provided" : project.getDescription()).append("\n");
        prompt.append("- Visibility: ").append(project.getVisibility()).append("\n");
        prompt.append("- Requested documentation type: ").append(type).append("\n\n");

        prompt.append("Repository metadata:\n");
        prompt.append("- Repository available: ").append(context.repositoryAvailable()).append("\n");
        prompt.append("- Repository name: ").append(context.repositoryName()).append("\n");
        prompt.append("- Primary language: ").append(context.primaryLanguage()).append("\n");
        prompt.append("- Total files: ").append(context.totalFiles()).append("\n");
        prompt.append("- Total folders: ").append(context.totalFolders()).append("\n");
        prompt.append("- Total size bytes: ").append(context.totalSizeBytes()).append("\n");
        prompt.append("- Language stats: ").append(context.languageStats()).append("\n\n");

        prompt.append(context.repositoryTree()).append("\n\n");

        prompt.append("""
            Documentation requirements:

            Include as much useful information as possible:
            1. Executive summary
            2. Project purpose
            3. Technology stack
            4. Folder structure explanation
            5. Module/package breakdown
            6. Important files and what they do
            7. Backend architecture, if backend exists
            8. Frontend architecture, if frontend exists
            9. Database/entity explanation, if detectable
            10. API endpoints, if detectable
            11. Authentication/security flow, if detectable
            12. Repository/code flow explanation
            13. Setup and installation guide
            14. Environment variables
            15. How to run locally
            16. How to build/deploy
            17. Main user flows
            18. Developer notes
            19. Possible improvements
            20. Known assumptions or missing information

            Important:
            - Be specific to the repository.
            - Reference real paths where possible.
            - Do not invent files that do not exist.
            - If something is not available, say "Not detected from repository context".
            """);

        if (includeFlowcharts) {
            prompt.append("""
                
                Flowchart requirements:
                Include Mermaid diagrams in Markdown.

                Add at least these diagrams when possible:

                1. Overall architecture:
                ```mermaid
                flowchart TD
                  User[User] --> Frontend[Frontend]
                  Frontend --> Backend[Backend API]
                  Backend --> Database[(Database)]
                  Backend --> AI[AI Provider]
                ```

                2. Main application flow:
                ```mermaid
                flowchart TD
                  A[Start] --> B[Register/Login]
                  B --> C[Dashboard]
                  C --> D[Create or Import Project]
                  D --> E[Upload/Parse Repository]
                  E --> F[Explore Files]
                  F --> G[Ask AI]
                  G --> H[Generate Documentation]
                ```

                3. Repository parsing flow
                4. AI question-answer flow
                5. Authentication flow if detectable

                Make Mermaid diagrams valid and readable.
                """);
        }

        if (additionalInstructions != null && !additionalInstructions.isBlank()) {
            prompt.append("\nAdditional user instructions:\n")
                    .append(additionalInstructions)
                    .append("\n");
        }

        prompt.append("""
            
            Final output format:
            # Project Documentation

            Use clear headings, tables, bullet points, and Mermaid diagrams.
            Make the documentation good enough to put in a README or technical handoff document.
            """);

        return prompt.toString();
    }

    private String buildTitle(String projectName, DocumentationType type) {
        return switch (type) {
            case FULL_DOCUMENTATION -> projectName + " - Full Project Documentation";
            case README -> projectName + " - README";
            case ARCHITECTURE -> projectName + " - Architecture Documentation";
            case FLOWCHART -> projectName + " - Flowcharts";
            case INSTALLATION -> projectName + " - Installation Guide";
            case API_DOCUMENTATION -> projectName + " - API Documentation";
            case DATABASE_DOCUMENTATION -> projectName + " - Database Documentation";
        };
    }
}