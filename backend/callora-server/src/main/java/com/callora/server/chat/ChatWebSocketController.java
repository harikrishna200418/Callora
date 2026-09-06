package com.callora.server.chat;

import com.callora.server.auth.security.CustomUserDetails;
import com.callora.server.chat.dto.SendMessageRequest;
import com.callora.server.common.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.time.ZonedDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/{conversationId}/send")
    public void sendMessage(@DestinationVariable UUID conversationId, 
                            @Payload SendMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        
        CustomUserDetails userDetails = getUserDetails(headerAccessor);
        
        // Build entity
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(userDetails.getUserId());
        message.setType(request.getType());
        message.setContent(request.getContent());
        message.setMediaUrl(request.getMediaUrl());
        message.setSentAt(ZonedDateTime.now());
        message.setStatus("SENT");
        
        Message saved = chatService.saveMessage(message);
        
        // Broadcast to conversation topic
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, saved);
    }
    
    @MessageMapping("/chat/typing")
    public void typingIndicator(@Payload TypingIndicator indicator, SimpMessageHeaderAccessor headerAccessor) {
        CustomUserDetails userDetails = getUserDetails(headerAccessor);
        
        // Ensure user can only send typing indicator for themselves
        TypingIndicator safeIndicator = new TypingIndicator(
                indicator.conversationId(), 
                userDetails.getUserId(), 
                indicator.isTyping()
        );
        
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + indicator.conversationId() + "/typing", 
                safeIndicator
        );
    }

    private CustomUserDetails getUserDetails(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken authToken) {
            if (authToken.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails;
            }
        }
        throw new org.springframework.security.access.AccessDeniedException("Unauthenticated WebSocket session");
    }

    public record TypingIndicator(UUID conversationId, UUID userId, boolean isTyping) {}
}
