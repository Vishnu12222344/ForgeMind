package com.forgemind.projects.entity;

import com.forgemind.ai.entity.AIConversation;
import com.forgemind.common.entity.BaseEntity;
import com.forgemind.documentation.entity.GeneratedDocument;
import com.forgemind.repositories.entity.SourceRepository;
import com.forgemind.users.entity.User;
import com.forgemind.workspaces.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Collection;
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
        name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_projects_workspace_slug",
                        columnNames = {"workspace_id", "slug"}
                )
        },
        indexes = {
                @Index(name = "idx_projects_workspace_id", columnList = "workspace_id"),
                @Index(name = "idx_projects_created_by_id", columnList = "created_by_id"),
                @Index(name = "idx_projects_name", columnList = "name"),
                @Index(name = "idx_projects_archived", columnList = "archived"),
                @Index(name = "idx_projects_favorite", columnList = "favorite")
        }
)
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 280)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;

    @Column(nullable = false)
    @Builder.Default
    private boolean archived = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean favorite = false;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<ProjectTag> tags = new LinkedHashSet<>();

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SourceRepository sourceRepository;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AIConversation> aiConversations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<GeneratedDocument> generatedDocuments = new LinkedHashSet<>();

    public void replaceTags(Collection<String> tagNames) {
        this.tags.clear();

        if (tagNames == null) {
            return;
        }

        tagNames.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase())
                .distinct()
                .limit(20)
                .forEach(tag -> {
                    ProjectTag projectTag = ProjectTag.builder()
                            .project(this)
                            .name(tag)
                            .build();

                    this.tags.add(projectTag);
                });
    }
}