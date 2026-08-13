package com.callora.server.auth;

import com.callora.server.auth.dto.AuthResponse;
import com.callora.server.auth.dto.LoginRequest;
import com.callora.server.auth.dto.RegisterRequest;
import com.callora.server.auth.security.JwtService;
import com.callora.server.auth.security.CustomUserDetails;
import com.callora.server.common.entity.User;
import com.callora.server.common.entity.UserSettings;
import com.callora.server.common.repository.UserRepository;
import com.callora.server.common.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.Builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .onlineStatus("ONLINE")
                .build();

        user = userRepository.save(user);

        UserSettings settings = UserSettings.Builder()
                .user(user)
                .batteryProtectionEnabled(true)
                .automaticCallEndEnabled(true)
                .criticalBatteryThreshold(7)
                .warningBatteryThreshold(10)
                .build();
        userSettingsRepository.save(settings);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.Builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.Builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
