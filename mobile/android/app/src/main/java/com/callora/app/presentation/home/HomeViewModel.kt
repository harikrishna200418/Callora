package com.callora.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callora.app.data.remote.SignalingClient
import com.callora.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val signalingClient: SignalingClient
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats: StateFlow<List<ChatPreview>> = _chats.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadChats()
        observeRealTimeMessages()
    }

    private fun observeRealTimeMessages() {
        viewModelScope.launch {
            signalingClient.chatMessages.collect { incomingMsg ->
                val currentChats = _chats.value.toMutableList()
                val index = currentChats.indexOfFirst { it.id == incomingMsg.conversationId }
                
                if (index != -1) {
                    val updatedChat = currentChats[index].copy(
                        lastMessage = incomingMsg.content,
                        time = "Just now",
                        unreadCount = currentChats[index].unreadCount + 1
                    )
                    currentChats.removeAt(index)
                    currentChats.add(0, updatedChat) // Bump to top
                    _chats.value = currentChats
                } else {
                    // Fetch chats again to get the new conversation info
                    loadChats()
                }
            }
        }
    }

    fun refreshChats() {
        _isRefreshing.value = true
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            // For now, hardcode a user ID. In a real app, get this from an AuthRepository or UserSession.
            val userId = "user-123"
            
            val result = chatRepository.getConversations(userId)
            result.onSuccess { conversations ->
                val previews = conversations.map {
                    ChatPreview(
                        id = it.id,
                        name = it.contactName ?: it.targetUser?.fullName ?: "Unknown",
                        lastMessage = "No recent messages",
                        time = "Just now",
                        unreadCount = 0,
                        isOnline = it.targetUser?.onlineStatus == "ONLINE"
                    )
                }
                _chats.value = previews
            }.onFailure {
                // If API fails (e.g. backend not running), fallback to empty or keep existing
                if (_chats.value.isEmpty()) {
                    _chats.value = emptyList() // Or some default mock data if needed for UI testing
                }
            }
            _isRefreshing.value = false
        }
    }
}
