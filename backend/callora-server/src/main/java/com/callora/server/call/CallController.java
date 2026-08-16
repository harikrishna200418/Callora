package com.callora.server.call;

import com.callora.server.call.CallHistory.CallType;
import com.callora.server.call.dto.CallSessionResponse;
import com.callora.server.call.dto.InitiateCallRequest;
import com.callora.server.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST entry points for call session creation.
 *
 * POST /api/call/voice   — create a VOICE call session, return the session id
 * POST /api/call/video   — create a VIDEO call session, return the session id
 *
 * The returned callId must be embedded in every subsequent SignalingMessage
 * so the WebSocket controller can update the call_history row.
 *
 * NOTE: /api/call/sms is intentionally removed — SMS OTP is a paid feature
 * (see architecture decision: zero-cost constraint, Task 8 uses free SMTP).
 */
@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @PostMapping("/voice")
    public ResponseEntity<CallSessionResponse> initiateVoiceCall(
            @RequestBody InitiateCallRequest request,
            Authentication auth) {

        UUID callerId = extractUserId(auth);
        CallSessionResponse session = callService.initiateCall(
                callerId, request.getCalleeId(), CallType.VOICE);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/video")
    public ResponseEntity<CallSessionResponse> initiateVideoCall(
            @RequestBody InitiateCallRequest request,
            Authentication auth) {

        UUID callerId = extractUserId(auth);
        CallSessionResponse session = callService.initiateCall(
                callerId, request.getCalleeId(), CallType.VIDEO);
        return ResponseEntity.ok(session);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UUID extractUserId(Authentication auth) {
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getUserId();
    }
}
