package com.callora.server.user;

import com.callora.server.auth.security.CustomUserDetails;
import com.callora.server.common.entity.UserSettings;
import com.callora.server.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserDtoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable UUID id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String profilePictureUrl,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                userService.updateProfile(principal.getUserId(), id, fullName, bio, profilePictureUrl));
    }

    @GetMapping("/{id}/settings")
    public ResponseEntity<UserSettings> getUserSettings(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserSettings(id));
    }

    @PutMapping("/{id}/settings")
    public ResponseEntity<UserSettings> updateSettings(
            @PathVariable UUID id,
            @RequestBody UserSettings settings,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                userService.updateSettings(principal.getUserId(), id, settings));
    }
}
