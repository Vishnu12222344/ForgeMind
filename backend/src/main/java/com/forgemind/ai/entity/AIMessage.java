package com.forgemind.ai.entity;

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
        name = "ai_messages",
        indexes = {
                @Index(name = "idx_ai_messages_conversation_id", columnList = "conversation_id"),
                @Index(name = "idx_ai_messages_role", columnList = "message_role")
        }
)
public class AIMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AIConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_role", nullable = false, length = 20)
    private AIMessageRole role;

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

    @Lob
    @Column(name = "referenced_files_json", columnDefinition = "LONGTEXT")
    private String referencedFilesJson;
}