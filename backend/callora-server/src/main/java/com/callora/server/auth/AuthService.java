package com.callora.server.auth;

import com.callora.server.auth.dto.AuthResponse;
import com.callora.server.auth.dto.LoginRequest;
import com.callora.server.auth.dto.RegisterRequest;
import com.callora.server.auth.security.CustomUserDetails;
import com.callora.server.auth.security.JwtService;
import com.callora.server.common.entity.User;
import com.callora.server.common.entity.UserSettings;
import com.callora.server.common.repository.UserRepository;
import com.callora.server.common.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${callora.jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .onlineStatus("ONLINE")
                .build();

        user = userRepository.save(user);

        UserSettings settings = UserSettings.builder()
                .user(user)
                .batteryProtectionEnabled(true)
                .automaticCallEndEnabled(true)
                .criticalBatteryThreshold(7)
                .warningBatteryThreshold(10)
                .readReceiptsEnabled(true)
                .lastSeenVisibility("EVERYONE")
                .profileVisibility("EVERYONE")
                .build();
        userSettingsRepository.save(settings);

        log.info("New user registered: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setOnlineStatus("ONLINE");
        userRepository.save(user);

        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshAccessToken(String rawRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(rawRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        User user = storedToken.getUser();
        // Rotate refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.debug("Refresh token rotated for user: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(rawRefreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String rawRefreshToken = UUID.randomUUID().toString();

        // Store refresh token (hash = raw token for simplicity; use BCrypt in high-security deployments)
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(rawRefreshToken)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC)
                        .plusSeconds(refreshTokenExpiry / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .userId(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
