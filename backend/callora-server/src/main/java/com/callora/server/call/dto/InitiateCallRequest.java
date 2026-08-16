package com.callora.server.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Request body for POST /api/call/voice and /api/call/video */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateCallRequest {
    /** UUID of the user being called */
    private UUID calleeId;
}
