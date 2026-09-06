package com.callora.server.user;

import com.callora.server.common.entity.User;
import com.callora.server.common.entity.UserSettings;
import com.callora.server.common.repository.UserRepository;
import com.callora.server.common.repository.UserSettingsRepository;
import com.callora.server.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public UserDto getUserDtoById(UUID id) {
        return toDto(getUserById(id));
    }

    public UserSettings getUserSettings(UUID userId) {
        return userSettingsRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Settings not found for user: " + userId));
    }

    @Transactional
    public UserDto updateProfile(UUID requestingUserId, UUID targetUserId,
                                  String fullName, String bio, String profilePictureUrl) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot modify another user's profile");
        }
        User user = getUserById(targetUserId);
        if (fullName != null) user.setFullName(fullName);
        if (bio != null) user.setBio(bio);
        if (profilePictureUrl != null) user.setProfilePictureUrl(profilePictureUrl);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserSettings updateSettings(UUID requestingUserId, UUID targetUserId,
                                        UserSettings settingsUpdates) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cannot modify another user's settings");
        }
        UserSettings settings = getUserSettings(targetUserId);
        settings.setBatteryProtectionEnabled(settingsUpdates.isBatteryProtectionEnabled());
        settings.setAutomaticCallEndEnabled(settingsUpdates.isAutomaticCallEndEnabled());
        settings.setCriticalBatteryThreshold(settingsUpdates.getCriticalBatteryThreshold());
        settings.setWarningBatteryThreshold(settingsUpdates.getWarningBatteryThreshold());
        settings.setReadReceiptsEnabled(settingsUpdates.isReadReceiptsEnabled());
        settings.setLastSeenVisibility(settingsUpdates.getLastSeenVisibility());
        settings.setProfileVisibility(settingsUpdates.getProfileVisibility());
        return userSettingsRepository.save(settings);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserService::toDto).toList();
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .onlineStatus(user.getOnlineStatus())
                .oauthProvider(user.getOauthProvider())
                .lastSeen(user.getLastSeen())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
