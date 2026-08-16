package com.callora.server.chat;

import com.callora.server.common.entity.Conversation;
import com.callora.server.common.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getConversations(@RequestParam UUID userId) {
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @PostMapping("/conversations/one-to-one")
    public ResponseEntity<Conversation> getOrCreateConversation(@RequestParam UUID user1Id, @RequestParam UUID user2Id) {
        return ResponseEntity.ok(chatService.getOrCreateOneToOneConversation(user1Id, user2Id));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(chatService.getConversationMessages(conversationId));
    }
}
