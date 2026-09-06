package com.callora.server.call;

import com.callora.server.call.CallHistory.TerminationReason;
import com.callora.server.call.dto.SignalingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket signaling relay for WebRTC.
 *
 * All messages are published to /app/call/signaling by the client.
 * The controller forwards them to the intended recipient and, for
 * state-changing events, updates the call_history row via CallService.
 *
 * Supported type values (defined on the client):
 *   CALL_INITIATED   — caller → callee: incoming call notification
 *   CALL_RINGING     — callee → caller: callee device received the alert
 *   CALL_ACCEPTED    — callee → caller: user picked up
 *   CALL_REJECTED    — callee → caller: user declined
 *   CALL_ENDED       — either peer: hung up normally
 *   CALL_TERMINATED  — either peer: ended due to battery threshold
 *   OFFER            — caller → callee: WebRTC SDP offer
 *   ANSWER           — callee → caller: WebRTC SDP answer
 *   ICE              — either peer: ICE candidate trickle
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CallWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallService callService;

    @MessageMapping("/call/signaling")
    public void processSignalingMessage(@Payload SignalingMessage message) {

        // 1. Always relay to the intended recipient first (low-latency path).
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId().toString(),
                "/queue/signaling",
                message
        );

        // 2. Persist state changes.
        handleStateTransition(message);
    }

    // ── State machine ────────────────────────────────────────────────────────

    private void handleStateTransition(SignalingMessage msg) {
        if (msg.getCallId() == null || msg.getType() == null) return;

        try {
            switch (msg.getType()) {
                case "CALL_INITIATED"  -> {
                    // Row already created by CallController; nothing extra needed here.
                    // This message just notifies the callee device.
                    log.info("[signaling] CALL_INITIATED callId={}", msg.getCallId());
                }
                case "CALL_RINGING"   -> callService.markRinging(msg.getCallId());
                case "CALL_ACCEPTED"  -> callService.markAccepted(msg.getCallId());
                case "CALL_REJECTED"  -> callService.markRejected(msg.getCallId());
                case "CALL_ENDED"     -> callService.markEnded(
                        msg.getCallId(), TerminationReason.NORMAL, null);
                case "CALL_TERMINATED" -> {
                    // Battery-triggered termination — extracts reason + level from payload.
                    callService.recordBatteryTermination(msg);
                }
                // OFFER, ANSWER, ICE are pure relay — no DB update needed.
                default -> log.debug("[signaling] relay-only type={} callId={}",
                        msg.getType(), msg.getCallId());
            }
        } catch (Exception ex) {
            // Never let a persistence failure break the relay path.
            log.error("[signaling] Failed to persist state for type={} callId={}: {}",
                    msg.getType(), msg.getCallId(), ex.getMessage());
        }
    }
}
