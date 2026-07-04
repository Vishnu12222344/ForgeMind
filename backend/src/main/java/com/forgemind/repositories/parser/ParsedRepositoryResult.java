package com.forgemind.repositories.parser;

import lombok.Builder;

import java.util.Map;

@Builder
public record ParsedRepositoryResult(
        long totalFiles,
        long totalFolders,
        long totalSizeBytes,
        String primaryLanguage,
        Map<String, Long> languageStats
) {
}