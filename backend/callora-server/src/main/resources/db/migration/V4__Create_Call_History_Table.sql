-- ---------------------------------------------------------------
-- V4: call_history table
-- Stores one row per call session.
-- termination_reason: NORMAL | BATTERY_WARNING | BATTERY_CRITICAL | REJECTED | FAILED
-- call_type: VOICE | VIDEO
-- status: INITIATED | RINGING | ACTIVE | ENDED | REJECTED | FAILED
-- ---------------------------------------------------------------

CREATE TABLE call_history (
    id               CHAR(36)     NOT NULL DEFAULT (UUID()),
    caller_id        CHAR(36)     NOT NULL,
    callee_id        CHAR(36)     NOT NULL,
    call_type        VARCHAR(10)  NOT NULL COMMENT 'VOICE | VIDEO',
    status           VARCHAR(20)  NOT NULL DEFAULT 'INITIATED'
                         COMMENT 'INITIATED | RINGING | ACTIVE | ENDED | REJECTED | FAILED',
    termination_reason VARCHAR(30) DEFAULT NULL
                         COMMENT 'NORMAL | BATTERY_WARNING | BATTERY_CRITICAL | REJECTED | FAILED',
    battery_level_at_end INT DEFAULT NULL
                         COMMENT 'Battery % recorded at call end; NULL if unknown',
    started_at       DATETIME(6)  DEFAULT NULL COMMENT 'Set when ACTIVE',
    ended_at         DATETIME(6)  DEFAULT NULL COMMENT 'Set when ENDED | REJECTED | FAILED',
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                         ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_call_caller FOREIGN KEY (caller_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_call_callee FOREIGN KEY (callee_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_call_caller (caller_id),
    INDEX idx_call_callee (callee_id),
    INDEX idx_call_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
