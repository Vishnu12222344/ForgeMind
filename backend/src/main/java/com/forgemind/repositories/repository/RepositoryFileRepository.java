package com.forgemind.repositories.repository;

import com.forgemind.repositories.entity.RepositoryFile;
import com.forgemind.repositories.entity.RepositoryFileType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, UUID> {

    List<RepositoryFile> findByRepository_IdOrderByPathAsc(UUID repositoryId);

    Optional<RepositoryFile> findByIdAndRepository_Id(UUID fileId, UUID repositoryId);

    boolean existsByRepository_IdAndPath(UUID repositoryId, String path);

    long countByRepository_IdAndType(UUID repositoryId, RepositoryFileType type);

    void deleteByRepository_Id(UUID repositoryId);

    List<RepositoryFile> findByRepository_IdAndTypeAndBinaryFileFalseOrderByPathAsc(
            UUID repositoryId,
            RepositoryFileType type,
            Pageable pageable
    );

    List<RepositoryFile> findByRepository_IdAndIdInAndTypeAndBinaryFileFalse(
            UUID repositoryId,
            List<UUID> ids,
            RepositoryFileType type
    );
}