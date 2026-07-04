CREATE TABLE users (
                       id CHAR(36) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255),
                       avatar_url VARCHAR(500),
                       bio TEXT,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email),
                       UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;