package com.example.petplace.presentation.feature.chat

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun ChatScreen(navController: NavController) {
    ChatListScreen(
        onChatClick = { chatRoomId ->
            navController.navigate("chatDetail/$chatRoomId")
        },
    )
}
