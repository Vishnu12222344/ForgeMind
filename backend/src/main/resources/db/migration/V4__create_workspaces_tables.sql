CREATE TABLE workspaces (
                            id CHAR(36) NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            slug VARCHAR(255) NOT NULL,
                            personal BOOLEAN NOT NULL DEFAULT TRUE,
                            owner_id CHAR(36) NOT NULL,
                            created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                            updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                            PRIMARY KEY (id),
                            UNIQUE KEY uk_workspaces_slug (slug),
                            INDEX idx_workspaces_owner_id (owner_id),

                            CONSTRAINT fk_workspaces_owner
                                FOREIGN KEY (owner_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE workspace_members (
                                   id CHAR(36) NOT NULL,
                                   workspace_id CHAR(36) NOT NULL,
                                   user_id CHAR(36) NOT NULL,
                                   `role` VARCHAR(20) NOT NULL DEFAULT 'OWNER',
                                   joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                                   PRIMARY KEY (id),
                                   UNIQUE KEY uk_workspace_members_workspace_user (workspace_id, user_id),
                                   INDEX idx_workspace_members_workspace_id (workspace_id),
                                   INDEX idx_workspace_members_user_id (user_id),

                                   CONSTRAINT fk_workspace_members_workspace
                                       FOREIGN KEY (workspace_id)
                                           REFERENCES workspaces(id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_workspace_members_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES users(id)
                                           ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;