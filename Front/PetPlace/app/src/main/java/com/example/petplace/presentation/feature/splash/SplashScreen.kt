package com.example.petplace.presentation.feature.splash

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.petplace.PetPlaceApp
import com.example.petplace.presentation.common.navigation.BottomNavItem
import com.example.petplace.presentation.common.theme.BackgroundColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log


@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val app = PetPlaceApp.getAppContext() as PetPlaceApp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Asset("splash.json")
        )
        val progress by animateLottieCompositionAsState(composition, iterations = 1)

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
    }

    LaunchedEffect(Unit) {
        delay(2000)

        val accessToken = app.getAccessToken()
        val refreshToken = app.getRefreshToken()

        val goHome = when {
            accessToken.isNullOrEmpty() -> {
                Log.d("Token", "SplashScreen: 비어있음 토큰")
                false
            }

            viewModel.isTokenValid(accessToken) -> {
                Log.d("Token", "SplashScreen: 유효한 토큰")
                true
            }

            !refreshToken.isNullOrEmpty() -> {
                Log.d("Token", "SplashScreen: 토큰 만료 → 리프레시 시도")
                viewModel.refreshToken(refreshToken)
            }

            else -> {
                Log.d("Token", "SplashScreen: 리프레시 토큰 없음 → 로그인 이동")
                false
            }
        }

        if (goHome) {
            navController.navigate(BottomNavItem.Feed.route) {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

}
