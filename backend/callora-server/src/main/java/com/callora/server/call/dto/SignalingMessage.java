package com.callora.server.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalingMessage {
    private String type; // OFFER, ANSWER, ICE, END, BATTERY_WARNING, CALL_TERMINATED
    private UUID callId;
    private UUID senderId;
    private UUID recipientId;
    private Object payload; // SDP string, ICE candidate object, or Battery event details
}
