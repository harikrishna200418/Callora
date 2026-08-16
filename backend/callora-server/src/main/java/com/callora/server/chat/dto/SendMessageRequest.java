package com.callora.server.chat.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String type; // TEXT, IMAGE, VIDEO, DOCUMENT, VOICE
    private String content;
    private String mediaUrl;
}
