-- ---------------------------------------------------------------
-- V4: call_history table
-- Stores one row per call session.
-- termination_reason: NORMAL | BATTERY_WARNING | BATTERY_CRITICAL | REJECTED | FAILED
-- call_type: VOICE | VIDEO
-- status: INITIATED | RINGING | ACTIVE | ENDED | REJECTED | FAILED
-- ---------------------------------------------------------------

CREATE TABLE call_history (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    caller_id        UUID NOT NULL,
    callee_id        UUID NOT NULL,
    call_type        VARCHAR(10) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    termination_reason VARCHAR(30),
    battery_level_at_end INTEGER,
    started_at       TIMESTAMP WITH TIME ZONE,
    ended_at         TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_call_caller FOREIGN KEY (caller_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_call_callee FOREIGN KEY (callee_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_call_caller ON call_history (caller_id);
CREATE INDEX idx_call_callee ON call_history (callee_id);
CREATE INDEX idx_call_created ON call_history (created_at);
