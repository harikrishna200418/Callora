package com.callora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.callora.app.presentation.auth.LoginScreen
import com.callora.app.presentation.calling.CallScreen
import com.callora.app.presentation.chat.ChatScreen
import com.callora.app.presentation.home.HomeNavigation
import com.callora.app.presentation.theme.CalloraTheme
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    data class Chat(val contactName: String) : Screen()
    data class Call(val contactName: String, val callId: String, val recipientId: String, val isCaller: Boolean) : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalloraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        )
    }

    when (val screen = currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginSuccess = {
                    currentScreen = Screen.Home
                }
            )
        }
        is Screen.Home -> {
            HomeNavigation(
                onOpenChat = { contactName ->
                    currentScreen = Screen.Chat(contactName)
                },
                onStartCall = { contactName ->
                    currentScreen = Screen.Call(
                        contactName = contactName,
                        callId = "call-${System.currentTimeMillis()}",
                        recipientId = "user-2",
                        isCaller = true
                    )
                },
                onLogout = {
                    currentScreen = Screen.Login
                }
            )
        }
        is Screen.Chat -> {
            ChatScreen(
                contactName = screen.contactName,
                onBack = {
                    currentScreen = Screen.Home
                },
                onStartCall = {
                    currentScreen = Screen.Call(
                        contactName = screen.contactName,
                        callId = "call-${System.currentTimeMillis()}",
                        recipientId = "user-2",
                        isCaller = true
                    )
                }
            )
        }
        is Screen.Call -> {
            CallScreen(
                contactName = screen.contactName,
                callId = screen.callId,
                recipientId = screen.recipientId,
                isCaller = screen.isCaller,
                onEndCall = {
                    currentScreen = Screen.Home
                }
            )
        }
    }
}
