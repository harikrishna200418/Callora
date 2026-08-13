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

@Composable
fun CallScreen(
    contactName: String,
    batteryWarningText: String? = null,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Mock Video View
        Text(
            text = "Video Stream of $contactName",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )

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
                onClick = { isVideoOn = !isVideoOn },
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
                onClick = onEndCall,
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
