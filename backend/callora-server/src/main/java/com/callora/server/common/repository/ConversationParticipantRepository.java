package com.callora.server.common.repository;

import com.callora.server.common.entity.ConversationParticipant;
import com.callora.server.common.entity.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {
    List<ConversationParticipant> findByUserId(UUID userId);
    List<ConversationParticipant> findByConversationId(UUID conversationId);
}
