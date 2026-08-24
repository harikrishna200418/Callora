package com.callora.server.call;

import com.callora.server.common.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent record of a single call session.
 * One row is created when the call is INITIATED and updated as it progresses.
 */
@Entity
@Table(name = "call_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallHistory {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callee_id", nullable = false)
    private User callee;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false, length = 10)
    private CallType callType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CallStatus status = CallStatus.INITIATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason", length = 30)
    private TerminationReason terminationReason;

    @Column(name = "battery_level_at_end")
    private Integer batteryLevelAtEnd;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }

    // ── Nested enums ──────────────────────────────────────────

    public enum CallType { VOICE, VIDEO }

    public enum CallStatus {
        INITIATED, RINGING, ACTIVE, ENDED, REJECTED, FAILED
    }

    public enum TerminationReason {
        NORMAL, BATTERY_WARNING, BATTERY_CRITICAL, REJECTED, FAILED
    }
}
