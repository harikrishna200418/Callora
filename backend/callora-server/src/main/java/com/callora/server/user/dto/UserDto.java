package com.callora.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Safe user projection — never exposes passwordHash or internal fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private String onlineStatus;
    private String oauthProvider;
    private ZonedDateTime lastSeen;
    private ZonedDateTime createdAt;
}
