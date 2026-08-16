package com.callora.app.presentation.calling

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callora.app.data.local.TokenManager
import com.callora.app.data.remote.SignalingClient
import com.callora.app.data.remote.SignalingMessage
import com.callora.app.data.remote.WebRTCClient
import com.callora.app.domain.usecase.CallTerminationController
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val webRTCClient: WebRTCClient,
    private val signalingClient: SignalingClient,
    private val callTerminationController: CallTerminationController,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val TAG = "CallViewModel"
    private val gson = Gson()

    private var currentCallId: String = ""
    private var myUserId: String = ""
    private var remoteUserId: String = ""

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private val _remoteVideoEnabled = MutableStateFlow(true)
    val remoteVideoEnabled: StateFlow<Boolean> = _remoteVideoEnabled.asStateFlow()

    private val _batteryWarning = MutableStateFlow<String?>(null)
    val batteryWarning: StateFlow<String?> = _batteryWarning.asStateFlow()

    private val _callEnded = MutableStateFlow(false)
    val callEnded: StateFlow<Boolean> = _callEnded.asStateFlow()

    init {
        setupBatteryMonitoring()
        setupWebRTCListeners()
        
        viewModelScope.launch {
            // My user id might come from token or another source, stub for now
            myUserId = "my-uuid-here" // TODO: Decode JWT or fetch from user service
            signalingClient.connect(myUserId)
            
            signalingClient.messages.collect { msg ->
                handleSignalingMessage(msg)
            }
        }
    }

    fun initCall(callId: String, recipientId: String, isCaller: Boolean) {
        currentCallId = callId
        remoteUserId = recipientId

        webRTCClient.startLocalVideo()
        _localVideoTrack.value = webRTCClient.localVideoTrack
        webRTCClient.createPeerConnection()

        if (isCaller) {
            webRTCClient.createOffer { desc ->
                sendSdpMessage("OFFER", desc)
            }
        }
    }

    private fun setupBatteryMonitoring() {
        callTerminationController.onCallTerminated = {
            _batteryWarning.value = "Call terminated due to critical battery."
            endCall()
            
            viewModelScope.launch {
                signalingClient.sendMessage(
                    SignalingMessage(
                        type = "CALL_TERMINATED",
                        callId = currentCallId,
                        senderId = myUserId,
                        recipientId = remoteUserId,
                        payload = mapOf("reason" to "BATTERY_CRITICAL")
                    )
                )
            }
        }
        callTerminationController.startMonitoring()
    }

    private fun setupWebRTCListeners() {
        webRTCClient.onIceCandidate = { candidate ->
            sendIceCandidate(candidate)
        }
        
        webRTCClient.onAddStream = { stream ->
            if (stream.videoTracks.isNotEmpty()) {
                _remoteVideoTrack.value = stream.videoTracks.first()
            }
        }
    }

    private fun handleSignalingMessage(msg: SignalingMessage) {
        if (msg.callId != currentCallId) return

        when (msg.type) {
            "OFFER" -> {
                val sdpData = msg.payload?.get("sdp") as? String ?: return
                val desc = SessionDescription(SessionDescription.Type.OFFER, sdpData)
                webRTCClient.handleRemoteOffer(desc) { answerDesc ->
                    sendSdpMessage("ANSWER", answerDesc)
                }
            }
            "ANSWER" -> {
                val sdpData = msg.payload?.get("sdp") as? String ?: return
                val desc = SessionDescription(SessionDescription.Type.ANSWER, sdpData)
                webRTCClient.handleRemoteAnswer(desc)
            }
            "ICE" -> {
                val candidateObj = msg.payload?.get("candidate") as? Map<*, *> ?: return
                val sdpMid = candidateObj["sdpMid"] as? String ?: return
                val sdpMLineIndex = (candidateObj["sdpMLineIndex"] as? Double)?.toInt() ?: return
                val sdp = candidateObj["sdp"] as? String ?: return
                
                val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                webRTCClient.addRemoteIceCandidate(candidate)
            }
            "VIDEO_STATE_CHANGED" -> {
                val isVideoEnabled = msg.payload?.get("enabled") as? Boolean ?: return
                _remoteVideoEnabled.value = isVideoEnabled
            }
            "CALL_TERMINATED", "CALL_ENDED" -> {
                endCall()
            }
        }
    }

    fun toggleVideo(enabled: Boolean) {
        webRTCClient.localVideoTrack?.setEnabled(enabled)
        viewModelScope.launch {
            signalingClient.sendMessage(
                SignalingMessage(
                    type = "VIDEO_STATE_CHANGED",
                    callId = currentCallId,
                    senderId = myUserId,
                    recipientId = remoteUserId,
                    payload = mapOf("enabled" to enabled)
                )
            )
        }
    }

    private fun sendSdpMessage(type: String, desc: SessionDescription) {
        viewModelScope.launch {
            signalingClient.sendMessage(
                SignalingMessage(
                    type = type,
                    callId = currentCallId,
                    senderId = myUserId,
                    recipientId = remoteUserId,
                    payload = mapOf("sdp" to desc.description)
                )
            )
        }
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        viewModelScope.launch {
            signalingClient.sendMessage(
                SignalingMessage(
                    type = "ICE",
                    callId = currentCallId,
                    senderId = myUserId,
                    recipientId = remoteUserId,
                    payload = mapOf(
                        "candidate" to mapOf(
                            "sdpMid" to candidate.sdpMid,
                            "sdpMLineIndex" to candidate.sdpMLineIndex,
                            "sdp" to candidate.sdp
                        )
                    )
                )
            )
        }
    }

    fun endCall() {
        webRTCClient.endCall()
        callTerminationController.stopMonitoring()
        viewModelScope.launch {
            signalingClient.disconnect()
        }
        _callEnded.value = true
    }

    override fun onCleared() {
        super.onCleared()
        endCall()
    }
}
