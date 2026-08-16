package com.callora.server.config;

import com.callora.server.auth.security.JwtService;
import com.callora.server.auth.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT frames using the existing JWT system.
 * The token is read from the STOMP "Authorization" header (not HTTP header).
 * On success, a Spring Security principal is attached to the WS session.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[WS] CONNECT rejected — missing or malformed Authorization header");
                throw new org.springframework.security.access.AccessDeniedException(
                        "WebSocket connection requires a valid Bearer token");
            }

            String jwt = authHeader.substring(7);

            try {
                String username = jwtService.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    log.warn("[WS] CONNECT rejected — invalid JWT for user: {}", username);
                    throw new org.springframework.security.access.AccessDeniedException("Invalid JWT token");
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);

                log.debug("[WS] CONNECT authenticated — user: {}", username);

            } catch (org.springframework.security.access.AccessDeniedException ex) {
                throw ex;
            } catch (Exception ex) {
                log.warn("[WS] CONNECT rejected — JWT validation error (details hidden for security)");
                throw new org.springframework.security.access.AccessDeniedException("JWT validation failed");
            }
        }

        return message;
    }
}
