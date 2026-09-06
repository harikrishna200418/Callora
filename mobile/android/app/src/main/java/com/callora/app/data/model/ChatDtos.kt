package com.callora.app.data.model

data class UserDto(
    val id: String,
    val username: String,
    val fullName: String?,
    val onlineStatus: String?
)

data class ContactDto(
    val id: String,
    val contactName: String,
    val targetUser: UserDto?
)

data class ConversationDto(
    val id: String,
    val name: String?,
    val isGroup: Boolean,
    val targetUser: UserDto?,
    val contactName: String?
)
