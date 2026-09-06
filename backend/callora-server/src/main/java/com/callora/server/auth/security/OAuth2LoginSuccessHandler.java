package com.callora.server.auth.security;

import com.callora.server.common.entity.User;
import com.callora.server.common.entity.UserSettings;
import com.callora.server.common.repository.UserRepository;
import com.callora.server.common.repository.UserSettingsRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final JwtService jwtService;

    @Value("${callora.oauth2.frontend-redirect-url:http://localhost:5173/oauth/callback}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 provider did not return an email address");
            return;
        }

        // Find existing user by email or create a new one
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Generate a unique username from email prefix
                    String baseUsername = email.split("@")[0];
                    String username = baseUsername;
                    int counter = 1;
                    while (userRepository.existsByUsername(username)) {
                        username = baseUsername + counter++;
                    }

                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .fullName(name)
                            .profilePictureUrl(picture)
                            .passwordHash("OAUTH2_USER") // Placeholder, not used for auth
                            .oauthProvider("GOOGLE")
                            .onlineStatus("ONLINE")
                            .build();

                    newUser = userRepository.save(newUser);

                    // Create default settings
                    UserSettings settings = UserSettings.builder()
                            .user(newUser)
                            .batteryProtectionEnabled(true)
                            .automaticCallEndEnabled(true)
                            .criticalBatteryThreshold(7)
                            .warningBatteryThreshold(10)
                            .readReceiptsEnabled(true)
                            .lastSeenVisibility("EVERYONE")
                            .profileVisibility("EVERYONE")
                            .build();
                    userSettingsRepository.save(settings);

                    return newUser;
                });

        // Generate JWT tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Redirect to frontend with tokens
        String redirectUrl = frontendRedirectUrl
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&username=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}
