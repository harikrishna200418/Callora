package com.callora.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import org.springframework.lang.NonNull;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${callora.cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsRaw;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        String[] origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        // /ws — primary endpoint (used by React client)
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .withSockJS();

        // /ws/chat — legacy endpoint alias
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(origins)
                .withSockJS();

        // /ws/signaling — WebRTC signaling
        registry.addEndpoint("/ws/signaling")
                .setAllowedOrigins(origins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Authenticate every CONNECT frame via JWT
        registration.interceptors(webSocketAuthInterceptor);
    }
}
