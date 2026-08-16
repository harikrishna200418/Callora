-- ============================================================
-- V1: Initial Schema — MySQL 8 Compatible
-- ============================================================

CREATE TABLE users (
    id          CHAR(36) NOT NULL DEFAULT (UUID()),
    phone       VARCHAR(20)  UNIQUE,
    email       VARCHAR(255) UNIQUE,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    full_name   VARCHAR(100),
    bio         TEXT,
    profile_picture_url VARCHAR(512),
    online_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    oauth_provider VARCHAR(50),
    last_seen   DATETIME(6),
    created_at  DATETIME(6) NOT NULL DEFAULT NOW(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT NOW(6) ON UPDATE NOW(6),
    PRIMARY KEY (id),
    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_settings (
    user_id                   CHAR(36) NOT NULL,
    battery_protection_enabled TINYINT(1) NOT NULL DEFAULT 1,
    automatic_call_end_enabled TINYINT(1) NOT NULL DEFAULT 1,
    critical_battery_threshold INTEGER NOT NULL DEFAULT 7,
    warning_battery_threshold  INTEGER NOT NULL DEFAULT 10,
    read_receipts_enabled      TINYINT(1) NOT NULL DEFAULT 1,
    last_seen_visibility       VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    profile_visibility         VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    PRIMARY KEY (user_id),
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE contacts (
    user_id         CHAR(36) NOT NULL,
    contact_user_id CHAR(36) NOT NULL,
    contact_name    VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (user_id, contact_user_id),
    CONSTRAINT fk_contacts_user    FOREIGN KEY (user_id)         REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_contacts_contact FOREIGN KEY (contact_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE blocked_users (
    blocker_id  CHAR(36) NOT NULL,
    blocked_id  CHAR(36) NOT NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (blocker_id, blocked_id),
    CONSTRAINT fk_blocked_blocker FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_blocked_blocked FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conversations (
    id          CHAR(36) NOT NULL DEFAULT (UUID()),
    type        VARCHAR(20) NOT NULL COMMENT 'ONE_TO_ONE or GROUP',
    created_at  DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conversation_participants (
    conversation_id CHAR(36) NOT NULL,
    user_id         CHAR(36) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at       DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_cp_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_user         FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE CASCADE,
    INDEX idx_cp_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
    id              CHAR(36) NOT NULL DEFAULT (UUID()),
    conversation_id CHAR(36) NOT NULL,
    sender_id       CHAR(36),
    type            VARCHAR(20) NOT NULL COMMENT 'TEXT, IMAGE, VIDEO, DOCUMENT, VOICE',
    content         TEXT,
    media_url       VARCHAR(512),
    status          VARCHAR(20) NOT NULL DEFAULT 'SENT' COMMENT 'SENT, DELIVERED, READ',
    sent_at         DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender       FOREIGN KEY (sender_id)       REFERENCES users(id)         ON DELETE SET NULL,
    INDEX idx_msg_conversation_sent (conversation_id, sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE calls (
    id              CHAR(36) NOT NULL DEFAULT (UUID()),
    conversation_id CHAR(36) NOT NULL,
    caller_id       CHAR(36),
    type            VARCHAR(20) NOT NULL COMMENT 'VOICE or VIDEO',
    started_at      DATETIME(6) NOT NULL DEFAULT NOW(6),
    ended_at        DATETIME(6),
    end_reason      VARCHAR(50) COMMENT 'MANUAL, LOW_BATTERY, NETWORK_ERROR',
    duration_seconds INTEGER,
    PRIMARY KEY (id),
    CONSTRAINT fk_calls_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_calls_caller       FOREIGN KEY (caller_id)       REFERENCES users(id)         ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE battery_events (
    id           CHAR(36) NOT NULL DEFAULT (UUID()),
    call_id      CHAR(36) NOT NULL,
    user_id      CHAR(36) NOT NULL,
    battery_level INTEGER NOT NULL,
    event_type   VARCHAR(50) NOT NULL COMMENT 'WARNING, CRITICAL, TERMINATED',
    occurred_at  DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_be_call FOREIGN KEY (call_id) REFERENCES calls(id) ON DELETE CASCADE,
    CONSTRAINT fk_be_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id         CHAR(36) NOT NULL DEFAULT (UUID()),
    user_id    CHAR(36) NOT NULL,
    type       VARCHAR(50) NOT NULL,
    title      VARCHAR(255),
    body       TEXT,
    is_read    TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id          CHAR(36) NOT NULL DEFAULT (UUID()),
    user_id     CHAR(36) NOT NULL,
    token_hash  VARCHAR(512) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    revoked     TINYINT(1) NOT NULL DEFAULT 0,
    created_at  DATETIME(6) NOT NULL DEFAULT NOW(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_rt_token (token_hash(255)),
    INDEX idx_rt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
