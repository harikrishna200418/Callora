package com.callora.app.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun HomeNavigation() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Chats", "Calls", "Profile")
    val icons = listOf(Icons.Filled.Message, Icons.Filled.Call, Icons.Filled.Person)

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> Text("Chats Screen Placeholder")
                1 -> Text("Calls History Placeholder")
                2 -> Text("Profile & Settings Placeholder")
            }
        }
    }
}
