package com.forgemind.projects.repository;

import com.forgemind.projects.entity.Project;
import com.forgemind.projects.entity.ProjectTag;
import com.forgemind.projects.entity.ProjectVisibility;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> belongsToWorkspaces(List<UUID> workspaceIds) {
        return (root, query, cb) -> {
            if (workspaceIds == null || workspaceIds.isEmpty()) {
                return cb.disjunction();
            }

            return root.get("workspace").get("id").in(workspaceIds);
        };
    }

    public static Specification<Project> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Project> archived(Boolean archived) {
        return (root, query, cb) -> {
            if (archived == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("archived"), archived);
        };
    }

    public static Specification<Project> favorite(Boolean favorite) {
        return (root, query, cb) -> {
            if (favorite == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("favorite"), favorite);
        };
    }

    public static Specification<Project> visibility(ProjectVisibility visibility) {
        return (root, query, cb) -> {
            if (visibility == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("visibility"), visibility);
        };
    }

    public static Specification<Project> hasTag(String tag) {
        return (root, query, cb) -> {
            if (tag == null || tag.isBlank()) {
                return cb.conjunction();
            }

            if (query != null) {
                query.distinct(true);
            }

            Join<Project, ProjectTag> tags = root.join("tags");

            return cb.equal(
                    cb.lower(tags.get("name")),
                    tag.trim().toLowerCase()
            );
        };
    }
}