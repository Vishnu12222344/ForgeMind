package com.forgemind.repositories.entity;

import com.forgemind.common.entity.BaseEntity;
import com.forgemind.projects.entity.Project;
import com.forgemind.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "source_repositories",
        indexes = {
                @Index(name = "idx_source_repositories_project_id", columnList = "project_id"),
                @Index(name = "idx_source_repositories_uploaded_by_id", columnList = "uploaded_by_id"),
                @Index(name = "idx_source_repositories_status", columnList = "status")
        }
)
public class SourceRepository extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepositoryStatus status;

    @Column(name = "total_files", nullable = false)
    private long totalFiles;

    @Column(name = "total_folders", nullable = false)
    private long totalFolders;

    @Column(name = "total_size_bytes", nullable = false)
    private long totalSizeBytes;

    @Column(name = "primary_language", length = 100)
    private String primaryLanguage;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<RepositoryFile> files = new LinkedHashSet<>();

    @Lob
    @Column(name = "language_stats_json", columnDefinition = "LONGTEXT")
    private String languageStatsJson;

    @Lob
    @Column(name = "parse_error", columnDefinition = "LONGTEXT")
    private String parseError;

    @Column(name = "parsed_at")
    private Instant parsedAt;
}