package com.callora.server.user;

import com.callora.server.common.entity.Contact;
import com.callora.server.common.entity.User;
import com.callora.server.common.repository.ContactRepository;
import com.callora.server.common.repository.UserRepository;
import com.callora.server.user.dto.AddContactRequest;
import com.callora.server.user.dto.ContactDto;
import com.callora.server.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public ContactDto addContact(UUID userId, AddContactRequest request) {
        if (contactRepository.existsByUserIdAndPhoneNumber(userId, request.getPhoneNumber())) {
            throw new IllegalArgumentException("Contact with this phone number already exists.");
        }

        User owner = userService.getUserById(userId);
        
        Contact contact = Contact.builder()
                .user(owner)
                .contactName(request.getContactName())
                .phoneNumber(request.getPhoneNumber())
                .build();
                
        contact = contactRepository.save(contact);
        return toDto(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactDto> getContacts(UUID userId) {
        List<Contact> contacts = contactRepository.findByUserIdOrderByContactNameAsc(userId);
        return contacts.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteContact(UUID userId, UUID contactId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found or access denied."));
        contactRepository.delete(contact);
    }

    private ContactDto toDto(Contact contact) {
        UserDto targetUser = null;
        if (contact.getPhoneNumber() != null) {
            targetUser = userRepository.findByPhone(contact.getPhoneNumber())
                    .map(UserService::toDto)
                    .orElse(null);
        }

        return ContactDto.builder()
                .id(contact.getId())
                .ownerId(contact.getUser().getId())
                .targetUser(targetUser)
                .contactName(contact.getContactName())
                .phoneNumber(contact.getPhoneNumber())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}
