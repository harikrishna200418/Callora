package com.callora.server.user;

import com.callora.server.common.entity.User;
import com.callora.server.common.entity.UserSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable UUID id, 
                                              @RequestParam(required = false) String fullName,
                                              @RequestParam(required = false) String bio,
                                              @RequestParam(required = false) String profilePictureUrl) {
        return ResponseEntity.ok(userService.updateProfile(id, fullName, bio, profilePictureUrl));
    }

    @GetMapping("/{id}/settings")
    public ResponseEntity<UserSettings> getUserSettings(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserSettings(id));
    }

    @PutMapping("/{id}/settings")
    public ResponseEntity<UserSettings> updateSettings(@PathVariable UUID id, @RequestBody UserSettings settings) {
        return ResponseEntity.ok(userService.updateSettings(id, settings));
    }
}
