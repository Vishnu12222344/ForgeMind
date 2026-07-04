package com.forgemind.projects.entity;

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
        name = "project_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_tags_project_name",
                        columnNames = {"project_id", "name"}
                )
        },
        indexes = {
                @Index(name = "idx_project_tags_project_id", columnList = "project_id"),
                @Index(name = "idx_project_tags_name", columnList = "name")
        }
)
public class ProjectTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 64)
    private String name;
}