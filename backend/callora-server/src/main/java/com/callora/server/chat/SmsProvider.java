package com.callora.server.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsProvider.class);

    public boolean sendSms(String fromNumber, String toNumber, String messageBody) {
        logger.info("Simulating SMS provider sending message...");
        logger.info("From: {}", fromNumber);
        logger.info("To: {}", toNumber);
        logger.info("Message: {}", messageBody);
        
        // Simulate a successful delivery 90% of the time, fail 10% for testing
        boolean success = Math.random() > 0.1;
        if (success) {
            logger.info("SMS delivered successfully.");
        } else {
            logger.warn("SMS delivery failed.");
        }
        
        return success;
    }
}
