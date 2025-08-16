package com.example.petplace.presentation.common.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.petplace.presentation.feature.Neighborhood.NeighborhoodScreen
import com.example.petplace.presentation.feature.alarm.AlarmScreen
import com.example.petplace.presentation.feature.chat.ChatScreen
import com.example.petplace.presentation.feature.chat.SingleChatScreen
import com.example.petplace.presentation.feature.createfeed.CreateFeedScreen
import com.example.petplace.presentation.feature.feed.BoardEditScreen
import com.example.petplace.presentation.feature.feed.FeedDetailScreen
import com.example.petplace.presentation.feature.feed.FeedScreen
import com.example.petplace.presentation.feature.hotel.AnimalSelectScreen
import com.example.petplace.presentation.feature.hotel.DateSelectionScreen
import com.example.petplace.presentation.feature.hotel.HotelDetailScreen
import com.example.petplace.presentation.feature.hotel.HotelListScreen
import com.example.petplace.presentation.feature.hotel.HotelSharedViewModel
import com.example.petplace.presentation.feature.hotel.ReservationCheckoutScreen
import com.example.petplace.presentation.feature.hotel.ReservationSuccessScreen
import com.example.petplace.presentation.feature.join.CertificationScreen
import com.example.petplace.presentation.feature.join.JoinScreen
import com.example.petplace.presentation.feature.join.JoinViewModel
import com.example.petplace.presentation.feature.join.KakaoCertificationScreen
import com.example.petplace.presentation.feature.join.KakaoJoinCheckScreen
import com.example.petplace.presentation.feature.join.KakaoJoinScreen
import com.example.petplace.presentation.feature.join.KakaoJoinViewModel
import com.example.petplace.presentation.feature.login.LoginScreen
import com.example.petplace.presentation.feature.missing_list.MissingListScreen
import com.example.petplace.presentation.feature.missing_register.FamilySelectScreen
import com.example.petplace.presentation.feature.missing_register.RegisterScreen
import com.example.petplace.presentation.feature.missing_report.MissingMapScreen
import com.example.petplace.presentation.feature.missing_report.MissingReportDetailScreen
import com.example.petplace.presentation.feature.missing_report.ReportScreen
import com.example.petplace.presentation.feature.mypage.MyCommentScreen
import com.example.petplace.presentation.feature.mypage.MyLikePostScreen
import com.example.petplace.presentation.feature.mypage.MyPageScreen
import com.example.petplace.presentation.feature.mypage.MyPostScreen
import com.example.petplace.presentation.feature.mypage.MyWalkScreen
import com.example.petplace.presentation.feature.mypage.MyCareScreen
import com.example.petplace.presentation.feature.mypage.PetProfileScreen
import com.example.petplace.presentation.feature.mypage.ProfileCompleteScreen
import com.example.petplace.presentation.feature.mypage.ProfileEditScreen
import com.example.petplace.presentation.feature.splash.SplashScreen
import com.example.petplace.presentation.feature.userprofile.UserProfileScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkAndCareScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkAndCareWriteScreen
import com.example.petplace.presentation.feature.walk_and_care.WalkPostDetailScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun MainScaffold(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val bottomNavRoutes = listOf(
        BottomNavItem.Feed.route,
        BottomNavItem.Neighborhood.route,
//        BottomNavItem.CreateFeed.route,
        BottomNavItem.Chat.route,
        BottomNavItem.MyPage.route
    )

    LaunchedEffect(navController) {
        // NavController가 준비된 후 FCM 처리
        delay(300) // NavGraph가 완전히 설정될 때까지 대기
        handleFCMNavigation(context, navController)
    }

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
            composable("splash") { SplashScreen(navController) }
            composable("login") { LoginScreen(navController) }
//            composable("join") { JoinScreen(navController, viewModel) }
            composable(BottomNavItem.Feed.route) { FeedScreen(navController = navController) }
            composable(BottomNavItem.CreateFeed.route) { CreateFeedScreen(navController = navController) }
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
            composable(
                route = "feedDetail/{feedId}",
                arguments = listOf(navArgument("feedId") { type = NavType.LongType })
            ) { backStackEntry ->
                val feedId = backStackEntry.arguments?.getLong("feedId") ?: 0
                FeedDetailScreen(
                    feedId = feedId,
                    navController = navController
                )
            }
            composable(
                route = "missingReportDetail/{missingReportId}",
                arguments = listOf(navArgument("missingReportId") { type = NavType.LongType })
            ) { backStackEntry ->
                val missingReportId = backStackEntry.arguments?.getLong("missingReportId") ?: 0L
                MissingReportDetailScreen(
                    missingReportId = missingReportId,
                    navController = navController
                )
            }


//            composable("board/write") { BoardWriteScreen(navController = navController) }
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
                route = "walk_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                WalkPostDetailScreen(navController = navController, postId = id) // ✅ 변경된 시그니처
            }
            composable("walk_write") {
                WalkAndCareWriteScreen(navController = navController)
            }
            composable("alarm") { AlarmScreen(navController = navController) }
            composable("feed") { FeedScreen(navController = navController) }

            composable("missing_report") { ReportScreen(navController) }
            composable("missing_map") { MissingMapScreen(navController) }
            composable("missing_register") { RegisterScreen(navController) }
            composable("family/select") { FamilySelectScreen(navController) }
            composable("walk_and_care") { WalkAndCareScreen(navController) }
//            composable("missing_list"){ MissingListScreen(navController) }
            composable("missing_list") {
                MissingListScreen(navController)
            }
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
            composable(
                route = "pet_profile?petId={petId}",
                arguments = listOf(
                    navArgument("petId") {
                        type = NavType.IntType
                        defaultValue = -1 // -1이면 새 등록, 양수면 수정
                    }
                )
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getInt("petId")
                val actualPetId = if (petId == -1) null else petId

                PetProfileScreen(
                    navController = navController,
                    petId = actualPetId
                )
            }
            composable(
                route = "pet_complete/{petId}",
                arguments = listOf(
                    navArgument("petId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getInt("petId")
                val actualPetId = if (petId == -1) null else petId

                ProfileCompleteScreen(
                    navController = navController,
                    petId = actualPetId
                )
            }
            composable("my_post") { MyPostScreen(navController) }
            composable("my_comment") { MyCommentScreen(navController) }
            composable("my_likePost") { MyLikePostScreen(navController) }
            composable("my_walk") { MyWalkScreen(navController) }

            composable("my_care") { MyCareScreen(navController) }

            composable(
                route = "userProfile/{userId}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId") ?: 0
                UserProfileScreen(
                    navController = navController,
                    userId = userId
                )
            }


//            composable("hotel"){AnimalSelectScreen(navController)}
//            composable("DateSelectionScreen"){DateSelectionScreen(navController)}
//            composable("HotelListScreen"){ HotelListScreen(navController) }
            navigation(startDestination = "hotel/date", route = "hotel_graph") {
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
                composable(
                    route = "hotel/success/{merchantUid}?rid={reservationId}",
                    arguments = listOf(
                        navArgument("merchantUid") { type = NavType.StringType },
                        navArgument("reservationId") {
                            type = NavType.LongType; defaultValue = -1L
                        } // 쿼리로 받음
                    )
                ) { backStackEntry ->
                    // 그래프 스코프의 ViewModel 유지
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("hotel_graph")
                    }
                    val viewModel = hiltViewModel<HotelSharedViewModel>(parentEntry)

                    val merchantUid = backStackEntry.arguments!!.getString("merchantUid")!!
                    val reservationId = backStackEntry.arguments!!.getLong("reservationId")

                    ReservationSuccessScreen(
                        navController = navController,
                        viewModel = viewModel,
                        merchantUid = merchantUid,
                        reservationId = reservationId
                    )
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

// FCM 네비게이션 처리 함수
private fun handleFCMNavigation(context: Context, navController: NavHostController) {
    val fcmDataPrefs = context.getSharedPreferences("fcm_data", Context.MODE_PRIVATE)
    val hasPendingFcm = fcmDataPrefs.getBoolean("fcm_pending", false)

    Log.d("FCM_NAV_SCAFFOLD", "Handling FCM navigation - hasPending: $hasPendingFcm")

    if (hasPendingFcm) {
        val fcmType = fcmDataPrefs.getString("fcm_type", null)
        val refType = fcmDataPrefs.getString("fcm_ref_type", null)
        val refId = fcmDataPrefs.getString("fcm_ref_id", null)
        val chatId = fcmDataPrefs.getString("fcm_chat_id", null)
        val userId = fcmDataPrefs.getString("fcm_user_id", null)

        Log.d("FCM_NAV_SCAFFOLD", "FCM navigation data - RefType: $refType, RefId: $refId, FCMType: $fcmType, ChatId: $chatId")

        // FCM 데이터 클리어
        fcmDataPrefs.edit().apply {
            remove("fcm_type")
            remove("fcm_ref_type")
            remove("fcm_ref_id")
            remove("fcm_chat_id")
            remove("fcm_user_id")
            remove("fcm_notification_id")
            remove("fcm_pending")
            // 백업 데이터도 클리어
            remove("has_fcm_data")
            remove("backup_refType")
            remove("backup_refId")
            remove("backup_type")
            remove("backup_chat_id")
            remove("backup_user_id")
            apply()
        }

        // 현재 경로 확인
        val currentRoute = navController.currentDestination?.route
        Log.d("FCM_NAV_SCAFFOLD", "Current route: $currentRoute")

        // splash나 login 화면에서는 메인 화면으로 이동 후 네비게이션
        if (currentRoute == "splash" || currentRoute == "login") {
            try {
                // 먼저 메인 화면으로 이동
                navController.navigate(BottomNavItem.Feed.route) {
                    popUpTo("splash") { inclusive = true }
                    launchSingleTop = true
                }

                // 잠시 후 타겟 화면으로 이동
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    performFCMNavigationInScaffold(navController, refType, refId, fcmType, chatId, userId)
                }
            } catch (e: Exception) {
                Log.e("FCM_NAV_SCAFFOLD", "Error during navigation from $currentRoute: ${e.message}")
            }
        } else {
            // 이미 메인 영역에 있으면 바로 네비게이션
            performFCMNavigationInScaffold(navController, refType, refId, fcmType, chatId, userId)
        }
    }
}

private fun performFCMNavigation(
    navController: NavHostController,
    refType: String?,
    refId: String?,
    fcmType: String?,
    chatId: String?
) {
    Log.d("FCM_NAV_DIRECT", "=== performFCMNavigation ===")
    Log.d("FCM_NAV_DIRECT", "refType: $refType, refId: $refId, fcmType: $fcmType, chatId: $chatId")

    // 채팅 관련 처리 강화
    val isChatType = refType?.equals("CHAT", ignoreCase = true) == true ||
            fcmType?.equals("chat", ignoreCase = true) == true

    if (isChatType) {
        // 채팅 ID 우선순위: refId -> chatId
        val chatIdToUse = refId ?: chatId
        Log.d("FCM_NAV_DIRECT", "Chat type detected. Using chatId: $chatIdToUse")

        chatIdToUse?.toLongOrNull()?.let { id ->
            Log.d("FCM_NAV_DIRECT", "Navigating to CHAT with ID: $id")
            try {
                // 채팅 페이지로 직접 네비게이션
                navController.navigate("chatDetail/$id") {
                    launchSingleTop = true
                    // 백스택 정리 (필요시)
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    restoreState = true
                }
                return // 성공적으로 네비게이션했으면 여기서 종료
            } catch (e: Exception) {
                Log.e("FCM_NAV_DIRECT", "Error navigating to chat detail: ${e.message}")
                // 채팅 상세 페이지 실패시 채팅 목록으로
                try {
                    navController.navigate("chat") {
                        launchSingleTop = true
                    }
                    return
                } catch (e2: Exception) {
                    Log.e("FCM_NAV_DIRECT", "Error navigating to chat list: ${e2.message}")
                }
            }
        } ?: run {
            Log.d("FCM_NAV_DIRECT", "No valid chat ID found, going to chat list")
            try {
                navController.navigate("chat") {
                    launchSingleTop = true
                }
                return
            } catch (e: Exception) {
                Log.e("FCM_NAV_DIRECT", "Error navigating to chat list: ${e.message}")
            }
        }
    }

    // 기존 로직 (피드 및 기타 타입 처리)
    when (refType?.uppercase()) {
        "FEED" -> {
            refId?.toLongOrNull()?.let { id ->
                Log.d("FCM_NAV_DIRECT", "Navigating to FEED with ID: $id")
                try {
                    navController.navigate("feedDetail/$id") {
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    Log.e("FCM_NAV_DIRECT", "Error navigating to feed: ${e.message}")
                    navController.navigate("feed")
                }
            } ?: run {
                Log.d("FCM_NAV_DIRECT", "No feed ID found, going to feed list")
                navController.navigate("feed")
            }
        }
        else -> {
            // fcmType으로 판단
            when (fcmType?.lowercase()) {
                "feed" -> {
                    refId?.toLongOrNull()?.let { id ->
                        Log.d("FCM_NAV_DIRECT", "Navigating to FEED (from fcmType) with ID: $id")
                        try {
                            navController.navigate("feedDetail/$id") {
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("FCM_NAV_DIRECT", "Error navigating to feed: ${e.message}")
                            navController.navigate("feed")
                        }
                    } ?: run {
                        Log.d("FCM_NAV_DIRECT", "No feed ID found, going to feed list")
                        navController.navigate("feed")
                    }
                }
                else -> {
                    Log.d("FCM_NAV_DIRECT", "Unknown type: $refType / $fcmType, going to main feed")
                    navController.navigate("feed")
                }
            }
        }
    }
}

// MainScaffold.kt의 performFCMNavigationInScaffold 메서드도 동일하게 수정

private fun performFCMNavigationInScaffold(
    navController: NavHostController,
    refType: String?,
    refId: String?,
    fcmType: String?,
    chatId: String?,
    userId: String?
) {
    Log.d("FCM_NAV_SCAFFOLD", "=== performFCMNavigationInScaffold ===")
    Log.d("FCM_NAV_SCAFFOLD", "refType: $refType, refId: $refId, fcmType: $fcmType, chatId: $chatId")

    try {
        // 채팅 관련 처리 강화
        val isChatType = refType?.equals("CHAT", ignoreCase = true) == true ||
                fcmType?.equals("chat", ignoreCase = true) == true

        if (isChatType) {
            // 채팅 ID 우선순위: refId -> chatId
            val chatIdToUse = refId ?: chatId
            Log.d("FCM_NAV_SCAFFOLD", "Chat type detected. Using chatId: $chatIdToUse")

            chatIdToUse?.toLongOrNull()?.let { id ->
                Log.d("FCM_NAV_SCAFFOLD", "Navigating to CHAT with ID: $id")
                navController.navigate("chatDetail/$id") {
                    launchSingleTop = true
                }
                return // 성공적으로 네비게이션했으면 종료
            } ?: run {
                Log.d("FCM_NAV_SCAFFOLD", "No valid chat ID found, going to chat list")
                navController.navigate(BottomNavItem.Chat.route)
                return
            }
        }

        // 피드 처리
        val isFeedType = refType?.equals("FEED", ignoreCase = true) == true ||
                fcmType?.equals("feed", ignoreCase = true) == true

        if (isFeedType) {
            refId?.toLongOrNull()?.let { id ->
                Log.d("FCM_NAV_SCAFFOLD", "Navigating to FEED with ID: $id")
                navController.navigate("feedDetail/$id") {
                    launchSingleTop = true
                }
                return
            } ?: run {
                Log.d("FCM_NAV_SCAFFOLD", "No feed ID found, going to feed list")
                navController.navigate(BottomNavItem.Feed.route)
                return
            }
        }

        // 실종신고 처리
        val isSightType = refType?.equals("SIGHTING", ignoreCase = true) == true ||
                fcmType?.equals("sighting", ignoreCase = true) == true

        if (isSightType) {
            refId?.toLongOrNull()?.let { id ->
                Log.d("FCM_NAV_SCAFFOLD", "Navigating to SIGHT with ID: $id")
                navController.navigate("missingReportDetail/$id") {
                    launchSingleTop = true
                }
                return
            } ?: run {
                Log.d("FCM_NAV_SCAFFOLD", "No sight ID found, going to missing list")
                navController.navigate("missing_list")
                return
            }
        }

        // 기타 타입 처리
        when (fcmType?.lowercase()) {
            "alarm", "notification" -> {
                Log.d("FCM_NAV_SCAFFOLD", "Navigating to alarm")
                navController.navigate("alarm")
            }
            "user_profile" -> {
                userId?.toLongOrNull()?.let { id ->
                    Log.d("FCM_NAV_SCAFFOLD", "Navigating to user profile: $id")
                    navController.navigate("userProfile/$id")
                }
            }
            else -> {
                Log.d("FCM_NAV_SCAFFOLD", "Unknown FCM type, navigating to main feed")
                navController.navigate(BottomNavItem.Feed.route)
            }
        }

    } catch (e: Exception) {
        Log.e("FCM_NAV_SCAFFOLD", "Error during FCM navigation: ${e.message}")
        // 에러 발생 시 안전하게 메인 화면으로
        try {
            navController.navigate(BottomNavItem.Feed.route)
        } catch (e2: Exception) {
            Log.e("FCM_NAV_SCAFFOLD", "Critical navigation error: ${e2.message}")
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
