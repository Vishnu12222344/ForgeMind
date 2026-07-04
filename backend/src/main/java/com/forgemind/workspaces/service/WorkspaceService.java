package com.forgemind.workspaces.service;

import com.forgemind.common.exception.ResourceNotFoundException;
import com.forgemind.users.entity.User;
import com.forgemind.workspaces.entity.Workspace;
import com.forgemind.workspaces.entity.WorkspaceMember;
import com.forgemind.workspaces.entity.WorkspaceRole;
import com.forgemind.workspaces.repository.WorkspaceMemberRepository;
import com.forgemind.workspaces.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    // Every user automatically gets exactly one personal workspace on registration.
    // This prepares the schema for future team/organization workspaces without migration pain.
    @Transactional
    public Workspace createPersonalWorkspace(User user) {
        String baseSlug = slugify(user.getUsername());
        String slug = baseSlug;
        int suffix = 1;
        while (workspaceRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix++;
        }

        Workspace workspace = Workspace.builder()
                .name((user.getFullName() != null ? user.getFullName() : user.getUsername()) + "'s Workspace")
                .slug(slug)
                .personal(true)
                .owner(user)
                .build();

        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceMemberRepository.save(member);

        return workspace;
    }

    public Workspace getPersonalWorkspace(UUID userId) {
        return workspaceRepository.findByOwnerIdAndPersonalTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal workspace not found"));
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase();
        return slug.replaceAll("^-+|-+$", "");
    }
}