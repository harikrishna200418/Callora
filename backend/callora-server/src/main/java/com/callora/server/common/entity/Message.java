package com.callora.server.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(nullable = false)
    private String type; // TEXT, IMAGE, VIDEO, DOCUMENT, VOICE

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(nullable = false)
    private String status = "SENT"; // SENT, DELIVERED, READ

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private ZonedDateTime sentAt;
}
