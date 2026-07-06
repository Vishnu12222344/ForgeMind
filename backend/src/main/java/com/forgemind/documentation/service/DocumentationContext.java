package com.forgemind.documentation.service;

import com.forgemind.ai.provider.AIContextFile;

import java.util.List;
import java.util.Map;

public record DocumentationContext(
        boolean repositoryAvailable,
        String repositoryName,
        String primaryLanguage,
        long totalFiles,
        long totalFolders,
        long totalSizeBytes,
        Map<String, Long> languageStats,
        String repositoryTree,
        List<AIContextFile> contextFiles
) {
}