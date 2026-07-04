package com.forgemind.repositories.entity;

import com.forgemind.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "repository_files",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_repository_files_repository_path",
                        columnNames = {"repository_id", "path"}
                )
        },
        indexes = {
                @Index(name = "idx_repository_files_repository_id", columnList = "repository_id"),
                @Index(name = "idx_repository_files_type", columnList = "file_type"),
                @Index(name = "idx_repository_files_language", columnList = "language")
        }
)
public class RepositoryFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private SourceRepository repository;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private RepositoryFileType type;

    // MySQL utf8mb4 index limit issue:
    // 512 chars * 4 bytes = 2048 bytes, safe under 3072 key limit.
    @Column(nullable = false, length = 512)
    private String path;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String extension;

    @Column(length = 100)
    private String language;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private int depth;

    @Column(name = "binary_file", nullable = false)
    private boolean binaryFile;

    @Column(name = "content_truncated", nullable = false)
    private boolean contentTruncated;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGTEXT")
    private String content;
}