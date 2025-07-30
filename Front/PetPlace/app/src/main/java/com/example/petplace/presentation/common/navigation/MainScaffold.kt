package com.example.petplace.presentation.common.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petplace.presentation.common.theme.PetPlaceTheme
import com.example.petplace.presentation.feature.board.BoardScreen
import com.example.petplace.presentation.feature.board.BoardWriteScreen
import com.example.petplace.presentation.feature.chat.ChatScreen
import com.example.petplace.presentation.feature.chat.SingleChatScreen
import com.example.petplace.presentation.feature.home.HomeScreen
import com.example.petplace.presentation.feature.join.JoinScreen
import com.example.petplace.presentation.feature.login.LoginScreen
import com.example.petplace.presentation.feature.map.MapScreen
import com.example.petplace.presentation.feature.mypage.MyPageScreen

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Board.route,
        BottomNavItem.Map.route,
        BottomNavItem.Chat.route,
        BottomNavItem.MyPage.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("join") { JoinScreen(navController) }
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Board.route) { BoardScreen(navController = navController) }
            composable(BottomNavItem.Map.route) { MapScreen() }
            composable(BottomNavItem.Chat.route) { ChatScreen(navController) }
            composable(BottomNavItem.MyPage.route) { MyPageScreen() }
            composable(
                route = "chatDetail/{chatName}",
                arguments = listOf(navArgument("chatName") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
                SingleChatScreen(
                    chatPartnerName = chatName,
                    navController = navController
                )
            }
            composable("board/write") { BoardWriteScreen(navController = navController) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScaffoldPreview() {
    PetPlaceTheme {
        MainScaffold()
    }
}
