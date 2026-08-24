package com.callora.server.user;

import com.callora.server.user.dto.AddContactRequest;
import com.callora.server.user.dto.ContactDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDto> addContact(@AuthenticationPrincipal Jwt jwt, 
                                                 @RequestBody AddContactRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(contactService.addContact(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ContactDto>> getContacts(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(contactService.getContacts(userId));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@AuthenticationPrincipal Jwt jwt, 
                                              @PathVariable UUID contactId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        contactService.deleteContact(userId, contactId);
        return ResponseEntity.noContent().build();
    }
}
