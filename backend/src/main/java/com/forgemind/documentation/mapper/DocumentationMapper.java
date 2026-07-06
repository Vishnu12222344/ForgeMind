package com.forgemind.documentation.mapper;

import com.forgemind.documentation.dto.GeneratedDocumentResponse;
import com.forgemind.documentation.dto.GeneratedDocumentSummaryResponse;
import com.forgemind.documentation.entity.GeneratedDocument;

public class DocumentationMapper {

    private DocumentationMapper() {
    }

    public static GeneratedDocumentResponse toResponse(GeneratedDocument document) {
        return GeneratedDocumentResponse.builder()
                .id(document.getId())
                .projectId(document.getProject().getId())
                .generatedById(document.getGeneratedBy().getId())
                .type(document.getType())
                .title(document.getTitle())
                .content(document.getContent())
                .promptTokens(document.getPromptTokens())
                .completionTokens(document.getCompletionTokens())
                .totalTokens(document.getTotalTokens())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public static GeneratedDocumentSummaryResponse toSummary(GeneratedDocument document) {
        return GeneratedDocumentSummaryResponse.builder()
                .id(document.getId())
                .projectId(document.getProject().getId())
                .type(document.getType())
                .title(document.getTitle())
                .totalTokens(document.getTotalTokens())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}