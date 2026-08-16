package com.callora.app.presentation.calling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
fun CallScreen(
    contactName: String,
    callId: String,
    recipientId: String,
    isCaller: Boolean,
    viewModel: CallViewModel = hiltViewModel(),
    onEndCall: () -> Unit
) {
    val localVideoTrack by viewModel.localVideoTrack.collectAsState()
    val remoteVideoTrack by viewModel.remoteVideoTrack.collectAsState()
    val remoteVideoEnabled by viewModel.remoteVideoEnabled.collectAsState()
    val batteryWarningText by viewModel.batteryWarning.collectAsState()
    val callEnded by viewModel.callEnded.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initCall(callId, recipientId, isCaller)
    }

    LaunchedEffect(callEnded) {
        if (callEnded) {
            onEndCall()
        }
    }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Remote Video
        if (remoteVideoEnabled) {
            remoteVideoTrack?.let { track ->
                AndroidView(
                    factory = { context ->
                        SurfaceViewRenderer(context).apply {
                            init(com.callora.app.data.remote.WebRTCClient(context).eglBaseContext, null)
                            setEnableHardwareScaler(true)
                            setMirror(false)
                            track.addSink(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                Text(
                    text = "Connecting to $contactName...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.VideocamOff,
                    contentDescription = "Video Disabled",
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "$contactName paused their video",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 80.dp)
                )
            }
        }

        // Local Video (PIP)
        localVideoTrack?.let { track ->
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(com.callora.app.data.remote.WebRTCClient(context).eglBaseContext, null)
                        setEnableHardwareScaler(true)
                        setMirror(true)
                        setZOrderMediaOverlay(true)
                        track.addSink(this)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(100.dp, 150.dp)
                    .background(Color.DarkGray)
            )
        }

        // Battery Warning Overlay
        if (batteryWarningText != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = batteryWarningText,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Call Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .background(Color.DarkGray, CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "Mute",
                    tint = Color.White
                )
            }

            // Video Toggle
            IconButton(
                onClick = { 
                    isVideoOn = !isVideoOn
                    viewModel.toggleVideo(isVideoOn)
                },
                modifier = Modifier
                    .background(Color.DarkGray, CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isVideoOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = "Video",
                    tint = Color.White
                )
            }

            // End Call
            IconButton(
                onClick = {
                    viewModel.endCall()
                    onEndCall()
                },
                modifier = Modifier
                    .background(Color.Red, CircleShape)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
