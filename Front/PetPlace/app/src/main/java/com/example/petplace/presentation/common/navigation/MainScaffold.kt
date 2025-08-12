package com.example.petplace.presentation.common.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.petplace.presentation.feature.Neighborhood.NeighborhoodScreen
import com.example.petplace.presentation.feature.chat.ChatScreen
import com.example.petplace.presentation.feature.chat.SingleChatScreen
import com.example.petplace.presentation.feature.feed.BoardEditScreen
import com.example.petplace.presentation.feature.feed.BoardWriteScreen
import com.example.petplace.presentation.feature.feed.FeedScreen
import com.example.petplace.presentation.feature.hotel.AnimalSelectScreen
import com.example.petplace.presentation.feature.hotel.DateSelectionScreen
import com.example.petplace.presentation.feature.hotel.HotelListScreen
import com.example.petplace.presentation.feature.hotel.HotelSharedViewModel
import com.example.petplace.presentation.feature.join.CertificationScreen
import com.example.petplace.presentation.feature.join.JoinScreen
import com.example.petplace.presentation.feature.join.JoinViewModel
import com.example.petplace.presentation.feature.join.KakaoCertificationScreen
import com.example.petplace.presentation.feature.join.KakaoJoinCheckScreen
import com.example.petplace.presentation.feature.join.KakaoJoinScreen
import com.example.petplace.presentation.feature.join.KakaoJoinViewModel
import com.example.petplace.presentation.feature.login.LoginScreen
import com.example.petplace.presentation.feature.missing_list.MissingListScreen
import com.example.petplace.presentation.feature.missing_report.MissingMapScreen
import com.example.petplace.presentation.feature.missing_report.ReportScreen
import com.example.petplace.presentation.feature.mypage.MyPageScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkAndCareScreen
import com.example.petplace.presentation.feature.hotel.HotelDetailScreen
import com.example.petplace.presentation.feature.hotel.ReservationCheckoutScreen
import com.example.petplace.presentation.feature.hotel.ReservationSuccessScreen
import com.example.petplace.presentation.feature.missing_register.FamilySelectScreen
import com.example.petplace.presentation.feature.missing_register.RegisterScreen
import com.example.petplace.presentation.feature.splash.SplashScreen
import com.example.petplace.presentation.feature.mypage.MyPostScreen
import com.example.petplace.presentation.feature.mypage.PetProfileScreen
import com.example.petplace.presentation.feature.mypage.ProfileCompleteScreen
import com.example.petplace.presentation.feature.mypage.ProfileEditScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkAndCareWriteScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkPostDetailScreen

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
                route = "chatDetail/{chatRoomId}",
                arguments = listOf(navArgument("chatRoomId") { type = NavType.LongType })
            ) { backStackEntry ->
                val chatRoomId = backStackEntry.arguments?.getLong("chatRoomId") ?: 0
                SingleChatScreen(
                    chatRoomId = chatRoomId,
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
            //api연동 전 임시
            composable(
                route = "walk_detail?" +
                        "category={category}&title={title}&body={body}&date={date}&time={time}&imageUrl={imageUrl}" +
                        "&name={name}&avatar={avatar}",
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType; defaultValue = "" },
                    navArgument("title")    { type = NavType.StringType; defaultValue = "" },
                    navArgument("body")     { type = NavType.StringType; defaultValue = "" },
                    navArgument("date")     { type = NavType.StringType; defaultValue = "" },
                    navArgument("time")     { type = NavType.StringType; defaultValue = "" },
                    navArgument("imageUrl") { type = NavType.StringType; defaultValue = "" },
                    navArgument("name")     { type = NavType.StringType; defaultValue = "" },      // ⬅️ 프로필 이름
                    navArgument("avatar")   { type = NavType.StringType; defaultValue = "" },      // ⬅️ 프로필 이미지
                )
            ) { backStackEntry ->
                val avatar: String? =
                    backStackEntry.arguments?.getString("avatar").takeUnless { it.isNullOrBlank() }

                WalkPostDetailScreen(
                    navController = navController,
                    category = backStackEntry.arguments?.getString("category") ?: "",
                    title    = backStackEntry.arguments?.getString("title")    ?: "",
                    body     = backStackEntry.arguments?.getString("body")     ?: "",
                    date     = backStackEntry.arguments?.getString("date")     ?: "",
                    time     = backStackEntry.arguments?.getString("time")     ?: "",
                    imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                    reporterName      = backStackEntry.arguments?.getString("name") ?: "",
                    reporterAvatarUrl = avatar
                )
            }
            composable("walk_write") {
                WalkAndCareWriteScreen(navController = navController)
            }


            composable("missing_report") { ReportScreen(navController) }
            composable("missing_map") { MissingMapScreen(navController) }
            composable("missing_register") { RegisterScreen(navController) }
            composable("family/select") { FamilySelectScreen(navController) }
            composable("walk_and_care") { WalkAndCareScreen(navController) }
            composable("missing_list"){ MissingListScreen(navController) }
            composable(
                route = "board/edit/{feedId}/{regionId}",
                arguments = listOf(
                    navArgument("feedId") { type = NavType.LongType },
                    navArgument("regionId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val feedId = backStackEntry.arguments?.getLong("feedId") ?: 0L
                val regionId = backStackEntry.arguments?.getLong("regionId") ?: 0L
                BoardEditScreen(
                    navController = navController,
                    feedId = feedId,
                    regionId = regionId
                )
            }

            composable("profile_edit") { ProfileEditScreen(navController) }
            composable("pet_profile") { PetProfileScreen(navController) }
            composable("pet_complete") { ProfileCompleteScreen(navController) }
            composable("my_post") { MyPostScreen(navController) }


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
                composable("hotel/detail") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("hotel_graph")
                    }
                    val viewModel = hiltViewModel<HotelSharedViewModel>(parentEntry)
                    HotelDetailScreen(navController, viewModel)
                }

                composable("hotel/checkout") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("hotel_graph")
                    }
                    val viewModel = hiltViewModel<HotelSharedViewModel>(parentEntry)
                    ReservationCheckoutScreen(navController, viewModel)
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

            navigation(
                route = "kakao_join_graph",
                startDestination = "kakao_join_check/{tempToken}"
            ) {
                composable(
                    route = "kakao_join_check/{tempToken}",
                    arguments = listOf(
                        navArgument("tempToken") { type = NavType.StringType }
                    )
                ) { backStackEntry ->

                    val parentEntry = navController.getBackStackEntry("kakao_join_graph")
                    val kakaoVm = hiltViewModel<KakaoJoinViewModel>(parentEntry)


                    val tmp = backStackEntry.arguments!!.getString("tempToken")!!
                    Log.d("tempToken", "MainScaffold: tmp = $tmp ")
                    KakaoJoinCheckScreen(
                        tempToken = tmp,
                        navController = navController,
                        viewModel = kakaoVm
                    )
                }
                // 인증
                composable("kakao_join_form") { backStackEntry ->
                    // 역시 여기서 parentEntry 로 같은 뷰모델을 가져옵니다.
                    val parentEntry = navController.getBackStackEntry("kakao_join_graph")
                    val kakaoVm = hiltViewModel<KakaoJoinViewModel>(parentEntry)

                    KakaoCertificationScreen(
                        viewModel = kakaoVm,
                        navController = navController
                    )
                }

                composable("kakao_join_main") { backStackEntry ->
                    // 역시 여기서 parentEntry 로 같은 뷰모델을 가져옵니다.
                    val parentEntry = navController.getBackStackEntry("kakao_join_graph")
                    val kakaoVm = hiltViewModel<KakaoJoinViewModel>(parentEntry)

                    KakaoJoinScreen(
                        viewModel = kakaoVm,
                        navController = navController
                    )
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
