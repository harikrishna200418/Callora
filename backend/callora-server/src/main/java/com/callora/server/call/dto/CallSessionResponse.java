package com.callora.server.call.dto;

import com.callora.server.call.CallHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response body returned by POST /api/call/voice and /api/call/video.
 * The callId is the session ID the Android client uses when connecting
 * to the WebSocket signaling channel (/app/call/signaling).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSessionResponse {
    private UUID callId;
    private UUID callerId;
    private UUID calleeId;
    private CallHistory.CallType callType;
    private CallHistory.CallStatus status;
    private Instant createdAt;
}
