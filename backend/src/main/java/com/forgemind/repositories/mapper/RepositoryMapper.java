package com.forgemind.repositories.mapper;

import com.forgemind.repositories.dto.RepositoryFileResponse;
import com.forgemind.repositories.dto.RepositoryResponse;
import com.forgemind.repositories.entity.RepositoryFile;
import com.forgemind.repositories.entity.SourceRepository;

import java.util.Map;

public class RepositoryMapper {

    private RepositoryMapper() {
    }

    public static RepositoryResponse toRepositoryResponse(
            SourceRepository repository,
            Map<String, Long> languageStats
    ) {
        return RepositoryResponse.builder()
                .id(repository.getId())
                .projectId(repository.getProject().getId())
                .uploadedById(repository.getUploadedBy().getId())
                .originalFilename(repository.getOriginalFilename())
                .status(repository.getStatus())
                .totalFiles(repository.getTotalFiles())
                .totalFolders(repository.getTotalFolders())
                .totalSizeBytes(repository.getTotalSizeBytes())
                .primaryLanguage(repository.getPrimaryLanguage())
                .languageStats(languageStats)
                .parseError(repository.getParseError())
                .parsedAt(repository.getParsedAt())
                .createdAt(repository.getCreatedAt())
                .updatedAt(repository.getUpdatedAt())
                .build();
    }

    public static RepositoryFileResponse toFileResponse(RepositoryFile file) {
        return RepositoryFileResponse.builder()
                .id(file.getId())
                .repositoryId(file.getRepository().getId())
                .type(file.getType())
                .path(file.getPath())
                .name(file.getName())
                .extension(file.getExtension())
                .language(file.getLanguage())
                .sizeBytes(file.getSizeBytes())
                .depth(file.getDepth())
                .binaryFile(file.isBinaryFile())
                .contentTruncated(file.isContentTruncated())
                .content(file.getContent())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}