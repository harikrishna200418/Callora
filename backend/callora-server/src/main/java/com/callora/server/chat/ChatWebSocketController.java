package com.callora.server.chat;

import com.callora.server.common.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{conversationId}/send")
    public void sendMessage(@DestinationVariable UUID conversationId, @Payload Message message) {
        // Save message to DB in a real implementation via ChatService
        
        // Broadcast to conversation topic
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, message);
    }
    
    @MessageMapping("/chat/typing")
    public void typingIndicator(@Payload TypingIndicator indicator) {
        messagingTemplate.convertAndSend("/topic/conversations/" + indicator.conversationId() + "/typing", indicator);
    }

    public record TypingIndicator(UUID conversationId, UUID userId, boolean isTyping) {}
}
