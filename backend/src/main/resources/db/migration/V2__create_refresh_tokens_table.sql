CREATE TABLE refresh_tokens (
                                id CHAR(36) NOT NULL,
                                user_id CHAR(36) NOT NULL,
                                token VARCHAR(500) NOT NULL,
                                expiry_date DATETIME(6) NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                                PRIMARY KEY (id),
                                UNIQUE KEY uk_refresh_tokens_token (token),
                                INDEX idx_refresh_tokens_user_id (user_id),

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;