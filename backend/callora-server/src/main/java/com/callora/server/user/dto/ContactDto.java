package com.callora.server.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {
    private UUID id;
    private UUID ownerId;
    private UserDto targetUser; // Null if user is not registered on Callora
    private String contactName;
    private String phoneNumber;
    private ZonedDateTime createdAt;
}
