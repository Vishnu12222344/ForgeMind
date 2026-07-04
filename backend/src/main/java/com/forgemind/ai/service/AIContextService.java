package com.forgemind.ai.service;

import com.forgemind.ai.provider.AIContextFile;
import com.forgemind.repositories.entity.RepositoryFile;
import com.forgemind.repositories.entity.RepositoryFileType;
import com.forgemind.repositories.entity.SourceRepository;
import com.forgemind.repositories.repository.RepositoryFileRepository;
import com.forgemind.repositories.repository.SourceRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIContextService {

    private final SourceRepositoryRepository sourceRepositoryRepository;
    private final RepositoryFileRepository repositoryFileRepository;

    public List<AIContextFile> collectContextFiles(UUID projectId, List<UUID> fileIds) {
        SourceRepository repository = sourceRepositoryRepository.findByProject_Id(projectId)
                .orElse(null);

        if (repository == null) {
            return List.of();
        }

        List<RepositoryFile> files;

        if (fileIds != null && !fileIds.isEmpty()) {
            files = repositoryFileRepository.findByRepository_IdAndIdInAndTypeAndBinaryFileFalse(
                    repository.getId(),
                    fileIds,
                    RepositoryFileType.FILE
            );
        } else {
            files = repositoryFileRepository.findByRepository_IdAndTypeAndBinaryFileFalseOrderByPathAsc(
                    repository.getId(),
                    RepositoryFileType.FILE,
                    PageRequest.of(0, 8)
            );
        }

        return files.stream()
                .filter(file -> file.getContent() != null && !file.getContent().isBlank())
                .limit(8)
                .map(file -> AIContextFile.builder()
                        .id(file.getId())
                        .path(file.getPath())
                        .name(file.getName())
                        .language(file.getLanguage())
                        .content(file.getContent())
                        .build()
                )
                .toList();
    }
}