package com.callora.app.data.remote

import android.util.Log
import com.callora.app.data.local.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton

data class SignalingMessage(
    val type: String,
    val callId: String,
    val senderId: String,
    val recipientId: String,
    val payload: Map<String, Any>? = null
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: String
)

@Singleton
class SignalingClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager
) {
    private val TAG = "SignalingClient"
    private var session: StompSession? = null
    private val gson = Gson()

    private val _messages = MutableSharedFlow<SignalingMessage>(extraBufferCapacity = 100)
    val messages = _messages.asSharedFlow()

    private val _chatMessages = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 100)
    val chatMessages = _chatMessages.asSharedFlow()

    suspend fun connect(userId: String) {
        if (session != null) return

        val token = tokenManager.getAccessToken() ?: return
        val wsClient = OkHttpWebSocketClient(okHttpClient)
        val stompClient = StompClient(wsClient)

        try {
            // For Android emulator pointing to local machine, use 10.0.2.2
            val url = "ws://10.0.2.2:8081/ws?token=$token"
            session = stompClient.connect(url)
            Log.d(TAG, "STOMP Connected")

            subscribeToSignaling()
            subscribeToChatMessages()
        } catch (e: Exception) {
            Log.e(TAG, "STOMP Connection failed", e)
        }
    }

    private suspend fun subscribeToSignaling() {
        session?.let { s ->
            val subscription = s.subscribeText("/user/queue/signaling")
            try {
                subscription.collect { frame ->
                    val payloadStr: String = if (frame is String) frame as String else frame.toString()
                    val message = gson.fromJson(payloadStr, SignalingMessage::class.java)
                    Log.d(TAG, "Received message: ${message.type}")
                    _messages.emit(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Subscription error", e)
            }
        }
    }

    private suspend fun subscribeToChatMessages() {
        session?.let { s ->
            val subscription = s.subscribeText("/user/queue/messages")
            try {
                subscription.collect { frame ->
                    val payloadStr: String = if (frame is String) frame as String else frame.toString()
                    val chatMessage = gson.fromJson(payloadStr, ChatMessage::class.java)
                    Log.d(TAG, "Received chat message from: ${chatMessage.senderId}")
                    _chatMessages.emit(chatMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chat Subscription error", e)
            }
        }
    }

    suspend fun sendMessage(message: SignalingMessage) {
        try {
            val json = gson.toJson(message)
            session?.sendText("/app/call/signaling", json)
            Log.d(TAG, "Sent message: \${message.type}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
        }
    }

    suspend fun disconnect() {
        session?.disconnect()
        session = null
    }
}
