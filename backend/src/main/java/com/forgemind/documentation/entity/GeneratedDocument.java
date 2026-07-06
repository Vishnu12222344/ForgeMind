package com.forgemind.documentation.entity;

import com.forgemind.common.entity.BaseEntity;
import com.forgemind.projects.entity.Project;
import com.forgemind.users.entity.User;
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
        name = "generated_documents",
        indexes = {
                @Index(name = "idx_generated_documents_project_id", columnList = "project_id"),
                @Index(name = "idx_generated_documents_user_id", columnList = "generated_by_id"),
                @Index(name = "idx_generated_documents_type", columnList = "document_type")
        }
)
public class GeneratedDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_id", nullable = false)
    private User generatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private long promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private long completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private long totalTokens = 0;
}