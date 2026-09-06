package com.callora.server.chat;

import com.callora.server.common.entity.Conversation;
import com.callora.server.common.entity.ConversationParticipant;
import com.callora.server.common.entity.ConversationParticipantId;
import com.callora.server.common.entity.Message;
import com.callora.server.common.entity.User;
import com.callora.server.common.repository.ConversationParticipantRepository;
import com.callora.server.common.repository.ConversationRepository;
import com.callora.server.common.repository.MessageRepository;
import com.callora.server.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    private final SmsProvider smsProvider;
    private final com.callora.server.common.repository.ContactRepository contactRepository;

    public List<Conversation> getUserConversations(UUID userId) {
        return participantRepository.findByUserId(userId).stream()
                .map(ConversationParticipant::getConversation)
                .collect(Collectors.toList());
    }

    public List<Message> getConversationMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    @Transactional
    public Conversation getOrCreateOneToOneConversation(UUID user1Id, UUID user2Id) {
        // Find existing (naive approach for demo: get all of user1, see if user2 is there)
        List<ConversationParticipant> user1Parts = participantRepository.findByUserId(user1Id);
        for (ConversationParticipant p1 : user1Parts) {
            Conversation conv = p1.getConversation();
            if ("ONE_TO_ONE".equals(conv.getType())) {
                boolean hasUser2 = participantRepository.findByConversationId(conv.getId()).stream()
                        .anyMatch(p2 -> p2.getUser().getId().equals(user2Id));
                if (hasUser2) return conv;
            }
        }
        
        // Create new
        Conversation conv = Conversation.builder().type("ONE_TO_ONE").build();
        conv = conversationRepository.save(conv);
        
        User u1 = userService.getUserById(user1Id);
        User u2 = userService.getUserById(user2Id);
        
        participantRepository.save(ConversationParticipant.builder()
                .id(new ConversationParticipantId(conv.getId(), u1.getId()))
                .conversation(conv).user(u1).role("MEMBER").build());
                
        participantRepository.save(ConversationParticipant.builder()
                .id(new ConversationParticipantId(conv.getId(), u2.getId()))
                .conversation(conv).user(u2).role("MEMBER").build());
                
        return conv;
    }

    @Transactional
    public Conversation getOrCreateSmsConversation(UUID userId, UUID contactId) {
        // Find existing SMS conversation
        List<ConversationParticipant> userParts = participantRepository.findByUserId(userId);
        for (ConversationParticipant p : userParts) {
            Conversation conv = p.getConversation();
            if ("SMS".equals(conv.getType()) && contactId.equals(conv.getContactId())) {
                return conv;
            }
        }

        // Create new
        Conversation conv = Conversation.builder().type("SMS").contactId(contactId).build();
        conv = conversationRepository.save(conv);

        User u = userService.getUserById(userId);

        participantRepository.save(ConversationParticipant.builder()
                .id(new ConversationParticipantId(conv.getId(), u.getId()))
                .conversation(conv).user(u).role("MEMBER").build());

        return conv;
    }
    
    @Transactional
    public Message saveMessage(Message message) {
        Conversation conv = conversationRepository.findById(message.getConversationId()).orElse(null);
        if (conv != null && "SMS".equals(conv.getType())) {
            User sender = userService.getUserById(message.getSenderId());
            com.callora.server.common.entity.Contact contact = contactRepository.findById(conv.getContactId()).orElse(null);
            
            if (contact != null) {
                message.setSenderNumber(sender.getVerifiedSenderNumber());
                message.setRecipientNumber(contact.getPhoneNumber());
                message.setDirection("OUTGOING");
                
                boolean sent = smsProvider.sendSms(message.getSenderNumber(), message.getRecipientNumber(), message.getContent());
                message.setStatus(sent ? "SENT" : "FAILED");
            }
        }
        return messageRepository.save(message);
    }
}
