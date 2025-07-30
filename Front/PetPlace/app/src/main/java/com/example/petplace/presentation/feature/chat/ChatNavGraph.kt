package com.example.petplace.presentation.feature.chat

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun ChatNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chatList") {
        composable("chatList") {
            ChatScreen(navController)
        }
        composable(
            "chatDetail/{chatName}",
            arguments = listOf(navArgument("chatName") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("chatName") ?: ""
            SingleChatScreen(chatPartnerName = name)
        }
    }
}
