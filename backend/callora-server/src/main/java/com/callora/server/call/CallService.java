package com.callora.server.call;

import com.callora.server.call.CallHistory.CallStatus;
import com.callora.server.call.CallHistory.CallType;
import com.callora.server.call.CallHistory.TerminationReason;
import com.callora.server.call.dto.CallSessionResponse;
import com.callora.server.call.dto.SignalingMessage;
import com.callora.server.common.entity.User;
import com.callora.server.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final CallHistoryRepository callHistoryRepository;
    private final UserRepository userRepository;

    // ── Session creation ──────────────────────────────────────────────────────

    /**
     * Called by CallController when the caller POSTs to /api/call/voice|video.
     * Creates a INITIATED call record and returns the session id.
     */
    @Transactional
    public CallSessionResponse initiateCall(UUID callerId, UUID calleeId, CallType callType) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found: " + callerId));
        User callee = userRepository.findById(calleeId)
                .orElseThrow(() -> new IllegalArgumentException("Callee not found: " + calleeId));

        CallHistory session = CallHistory.builder()
                .caller(caller)
                .callee(callee)
                .callType(callType)
                .status(CallStatus.INITIATED)
                .build();

        callHistoryRepository.save(session);
        log.info("Call session INITIATED: id={} caller={} callee={} type={}",
                session.getId(), callerId, calleeId, callType);

        return toResponse(session);
    }

    // ── State transitions (driven by WebSocket signaling events) ─────────────

    /** Callee device received the incoming call notification. */
    @Transactional
    public void markRinging(UUID callId) {
        updateStatus(callId, CallStatus.RINGING, null, null);
    }

    /** Callee accepted — both peers begin WebRTC negotiation. */
    @Transactional
    public void markAccepted(UUID callId) {
        CallHistory call = findOrThrow(callId);
        call.setStatus(CallStatus.ACTIVE);
        call.setStartedAt(Instant.now());
        callHistoryRepository.save(call);
        log.info("Call ACTIVE: id={}", callId);
    }

    /** Callee rejected the call. */
    @Transactional
    public void markRejected(UUID callId) {
        updateStatus(callId, CallStatus.REJECTED, TerminationReason.REJECTED, null);
    }

    /**
     * Call ended — either normally or triggered by battery level.
     * batteryLevel may be null when the termination was not battery-related.
     */
    @Transactional
    public void markEnded(UUID callId, TerminationReason reason, Integer batteryLevel) {
        CallHistory call = findOrThrow(callId);
        call.setStatus(CallStatus.ENDED);
        call.setTerminationReason(reason != null ? reason : TerminationReason.NORMAL);
        call.setEndedAt(Instant.now());
        if (batteryLevel != null) {
            call.setBatteryLevelAtEnd(batteryLevel);
        }
        callHistoryRepository.save(call);
        log.info("Call ENDED: id={} reason={} battery={}%", callId, call.getTerminationReason(), batteryLevel);
    }

    // ── Battery-termination helper ────────────────────────────────────────────

    /**
     * Convenience wrapper called from the WebSocket controller when a
     * CALL_TERMINATED signaling event carries a battery context.
     */
    @Transactional
    public void recordBatteryTermination(SignalingMessage message) {
        if (message.getCallId() == null) {
            log.warn("CALL_TERMINATED event received without callId — skipping persistence");
            return;
        }

        TerminationReason reason = TerminationReason.NORMAL;
        Integer batteryLevel = null;

        if (message.getPayload() instanceof java.util.Map<?, ?> payload) {
            Object reasonVal = payload.get("reason");
            Object levelVal  = payload.get("batteryLevel");

            if ("BATTERY_CRITICAL".equals(reasonVal)) reason = TerminationReason.BATTERY_CRITICAL;
            else if ("BATTERY_WARNING".equals(reasonVal)) reason = TerminationReason.BATTERY_WARNING;

            if (levelVal instanceof Number n) batteryLevel = n.intValue();
        }

        markEnded(message.getCallId(), reason, batteryLevel);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private CallHistory findOrThrow(UUID callId) {
        return callHistoryRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Call session not found: " + callId));
    }

    private void updateStatus(UUID callId, CallStatus status,
                               TerminationReason reason, Instant endTime) {
        CallHistory call = findOrThrow(callId);
        call.setStatus(status);
        if (reason != null)  call.setTerminationReason(reason);
        if (endTime != null) call.setEndedAt(endTime);
        else if (status == CallStatus.ENDED || status == CallStatus.REJECTED)
            call.setEndedAt(Instant.now());
        callHistoryRepository.save(call);
        log.info("Call {}: id={}", status, callId);
    }

    private CallSessionResponse toResponse(CallHistory c) {
        return CallSessionResponse.builder()
                .callId(c.getId())
                .callerId(c.getCaller().getId())
                .calleeId(c.getCallee().getId())
                .callType(c.getCallType())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
