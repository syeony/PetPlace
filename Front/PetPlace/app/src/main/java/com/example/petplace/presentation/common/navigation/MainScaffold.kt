package com.example.petplace.presentation.common.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petplace.presentation.common.theme.PetPlaceTheme
import com.example.petplace.presentation.feature.missing_register.FamilySelectScreen
import com.example.petplace.presentation.feature.missing_register.RegisterScreen
import com.example.petplace.presentation.feature.Neighborhood.NeighborhoodScreen
import com.example.petplace.presentation.feature.chat.ChatScreen
import com.example.petplace.presentation.feature.chat.SingleChatScreen
import com.example.petplace.presentation.feature.feed.BoardWriteScreen
import com.example.petplace.presentation.feature.feed.FeedScreen
import com.example.petplace.presentation.feature.join.JoinScreen
import com.example.petplace.presentation.feature.hotel.AnimalSelectScreen
import com.example.petplace.presentation.feature.hotel.DateSelectionScreen
import com.example.petplace.presentation.feature.hotel.HotelListScreen
import com.example.petplace.presentation.feature.hotel.HotelSharedViewModel
import com.example.petplace.presentation.feature.login.LoginScreen
import com.example.petplace.presentation.feature.missing_list.MissingListScreen
import com.example.petplace.presentation.feature.missing_report.MissingMapScreen
import com.example.petplace.presentation.feature.missing_report.ReportScreen
import com.example.petplace.presentation.feature.mypage.MyPageScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkAndCareScreen
import androidx.navigation.navigation
import com.example.petplace.presentation.feature.join.CertificationScreen
import com.example.petplace.presentation.feature.join.JoinViewModel
import com.example.petplace.presentation.feature.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedGetBackStackEntry")
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
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash"){ SplashScreen(navController) }
            composable("login") { LoginScreen(navController) }
//            composable("join") { JoinScreen(navController, viewModel) }
            composable(BottomNavItem.Feed.route) { FeedScreen(navController = navController) }
            composable(BottomNavItem.Chat.route) { ChatScreen(navController) }
            composable(BottomNavItem.MyPage.route) { MyPageScreen(navController) }
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
            composable("missing_register") { RegisterScreen(navController) }
            composable("family/select") { FamilySelectScreen(navController) }
            composable("walk_and_care") { WalkAndCareScreen(navController) }
            composable("missing_list"){ MissingListScreen(navController) }
//            composable("hotel"){AnimalSelectScreen(navController)}
//            composable("DateSelectionScreen"){DateSelectionScreen(navController)}
//            composable("HotelListScreen"){ HotelListScreen(navController) }
            navigation(startDestination = "hotel/animal", route = "hotel_graph") {
                composable("hotel/animal") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("hotel_graph")
                    }
                    val viewModel = hiltViewModel<HotelSharedViewModel>(parentEntry)
                    AnimalSelectScreen(navController, viewModel)
                }
                composable("hotel/date") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("hotel_graph")
                    }
                    val viewModel = hiltViewModel<HotelSharedViewModel>(parentEntry)
                    DateSelectionScreen(navController, viewModel)
                }
                composable("hotel/list") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("hotel_graph")
                    }
                    val viewModel = hiltViewModel<HotelSharedViewModel>(parentEntry)
                    HotelListScreen(navController, viewModel)
                }
            }


            navigation(startDestination = "join/certification", route = "join_graph") {
                composable("join/certification") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("join_graph")
                    }
                    val viewModel = hiltViewModel<JoinViewModel>(parentEntry)
                    CertificationScreen(navController, viewModel)
                }
                composable("join/main") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("join_graph")
                    }
                    val viewModel = hiltViewModel<JoinViewModel>(parentEntry)
                    JoinScreen(navController, viewModel)
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainScaffoldPreview() {
//    PetPlaceTheme {
//        MainScaffold()
//    }
//}
