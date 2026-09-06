package com.callora.app.data.remote.api

import com.callora.app.data.model.ContactDto
import com.callora.app.data.model.ConversationDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatApi {
    @GET("/api/chat/conversations")
    suspend fun getConversations(@Query("userId") userId: String): List<ConversationDto>

    @POST("/api/chat/conversations/one-to-one")
    suspend fun createOneToOneConversation(
        @Query("user1Id") user1Id: String,
        @Query("user2Id") user2Id: String
    ): ConversationDto
}

interface ContactsApi {
    @GET("/api/contacts")
    suspend fun getContacts(): List<ContactDto>
}
