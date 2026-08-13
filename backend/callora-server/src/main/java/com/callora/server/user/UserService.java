package com.callora.server.user;

import com.callora.server.common.entity.User;
import com.callora.server.common.entity.UserSettings;
import com.callora.server.common.repository.UserRepository;
import com.callora.server.common.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    
    public UserSettings getUserSettings(UUID userId) {
        return userSettingsRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Settings not found"));
    }

    @Transactional
    public User updateProfile(UUID userId, String fullName, String bio, String profilePictureUrl) {
        User user = getUserById(userId);
        if (fullName != null) user.setFullName(fullName);
        if (bio != null) user.setBio(bio);
        if (profilePictureUrl != null) user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }
    
    @Transactional
    public UserSettings updateSettings(UUID userId, UserSettings settingsUpdates) {
        UserSettings settings = getUserSettings(userId);
        settings.setBatteryProtectionEnabled(settingsUpdates.isBatteryProtectionEnabled());
        settings.setAutomaticCallEndEnabled(settingsUpdates.isAutomaticCallEndEnabled());
        settings.setCriticalBatteryThreshold(settingsUpdates.getCriticalBatteryThreshold());
        settings.setWarningBatteryThreshold(settingsUpdates.getWarningBatteryThreshold());
        settings.setReadReceiptsEnabled(settingsUpdates.isReadReceiptsEnabled());
        settings.setLastSeenVisibility(settingsUpdates.getLastSeenVisibility());
        settings.setProfileVisibility(settingsUpdates.getProfileVisibility());
        return userSettingsRepository.save(settings);
    }
}
