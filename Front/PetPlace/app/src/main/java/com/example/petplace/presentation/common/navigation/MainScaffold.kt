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
import com.example.petplace.presentation.feature.Missing_register.FamilySelectScreen
import com.example.petplace.presentation.feature.Missing_register.RegisterScreen
import com.example.petplace.presentation.feature.Neighborhood.NeighborhoodScreen
import com.example.petplace.presentation.feature.chat.ChatScreen
import com.example.petplace.presentation.feature.chat.SingleChatScreen
import com.example.petplace.presentation.feature.feed.BoardWriteScreen
import com.example.petplace.presentation.feature.feed.FeedScreen
import com.example.petplace.presentation.feature.join.JoinScreen
import com.example.petplace.presentation.feature.login.LoginScreen
import com.example.petplace.presentation.feature.missing_report.MissingMapScreen
import com.example.petplace.presentation.feature.missing_report.ReportScreen
import com.example.petplace.presentation.feature.mypage.MyPageScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkAndCareScreen

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        BottomNavItem.Feed.route,
        BottomNavItem.Neighborhood.route,
        BottomNavItem.Chat.route,
        BottomNavItem.MyPage.route
    )

    Scaffold(
        bottomBar = {
            val showBottom = bottomNavRoutes.any { route ->
                currentRoute?.startsWith(route) == true   // ← startsWith 로 비교
            }
            if (showBottom) BottomBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("join") { JoinScreen(navController) }
            composable(BottomNavItem.Feed.route) { FeedScreen(navController = navController) }
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
            composable(
                route = "${BottomNavItem.Neighborhood.route}?showDialog={showDialog}",
                arguments = listOf(
                    navArgument("showDialog") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val showDialog = backStackEntry.arguments?.getBoolean("showDialog") ?: false
                NeighborhoodScreen(
                    navController = navController,
                    initialShowDialog = showDialog
                )
            }
            composable("missing_report") { ReportScreen(navController) }
            composable("missing_map") { MissingMapScreen(navController) }
            composable("Missing_register") { RegisterScreen(navController) }
            composable("family/select") { FamilySelectScreen(navController) }
            composable("walk_and_care") { WalkAndCareScreen(navController) }

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
