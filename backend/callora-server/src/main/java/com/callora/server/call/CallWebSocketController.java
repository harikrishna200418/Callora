package com.callora.server.call;

import com.callora.server.call.dto.SignalingMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CallWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call/signaling")
    public void processSignalingMessage(@Payload SignalingMessage message) {
        // Forward the signaling message (offer, answer, ICE) to the specific recipient
        messagingTemplate.convertAndSendToUser(
                message.getRecipientId().toString(), 
                "/queue/signaling", 
                message
        );
        
        // If it's a battery termination event, we should also log it to the database
        if ("CALL_TERMINATED".equals(message.getType())) {
            // Log to battery_events table via CallService
        }
    }
}
