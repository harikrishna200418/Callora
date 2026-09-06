package com.callora.app.data.repository

import com.callora.app.data.model.ConversationDto
import com.callora.app.data.remote.api.ChatApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApi: ChatApi
) {
    suspend fun getConversations(userId: String): Result<List<ConversationDto>> {
        return try {
            val conversations = chatApi.getConversations(userId)
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
