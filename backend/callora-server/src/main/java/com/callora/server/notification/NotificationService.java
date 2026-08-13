package com.callora.server.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    /**
     * In a production environment, this would integrate with Firebase Admin SDK (FCM)
     * to send push notifications to registered device tokens.
     */
    public void sendPushNotification(UUID userId, String title, String body, String type) {
        log.info("Sending push notification to user {}: Title='{}', Body='{}', Type='{}'", 
                userId, title, body, type);
        
        // Example implementation logic:
        // 1. Fetch user's active device tokens from database
        // 2. Build FCM Message
        // 3. FirebaseMessaging.getInstance().sendAsync(message)
    }
    
    public void sendIncomingCallNotification(UUID recipientId, UUID callerId, String callerName, boolean isVideo) {
        String title = "Incoming " + (isVideo ? "Video" : "Voice") + " Call";
        String body = callerName + " is calling you.";
        sendPushNotification(recipientId, title, body, "CALL");
    }

    public void sendMessageNotification(UUID recipientId, String senderName, String messagePreview) {
        sendPushNotification(recipientId, senderName, messagePreview, "MESSAGE");
    }
}
