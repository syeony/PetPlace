package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem
import com.example.petplace.presentation.common.theme.PrimaryColor

@Composable
fun ProfileCompleteScreen(
    navController: NavController,
    petId: Int? = null,
    viewModel: PetProfileViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    // petId가 있으면 펫 정보 로드
    LaunchedEffect(petId) {
        petId?.let {
            viewModel.loadPetInfo(it)
        }
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.animation)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        speed = 1f,
        isPlaying = true,
        restartOnPlay = false,
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier.fillMaxSize(),              // 전체 크기로 변경
            contentScale = ContentScale.Crop                // 비율 유지하면서 crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
        ) {
            Text(
                text = "프로필 등록 완료!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.TopEnd) {
                if (uiState.profileImageUri != null) {
                    val imageUrl = uiState.profileImageUri.toString().let { url ->
                        if (url.startsWith("http")) {
                            url
                        } else {
                            "http://43.201.108.195:8081$url"  // 서버 베이스 URL 붙이기
                        }
                    }
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.sample_hamster),
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = uiState.petName.ifEmpty { "기쁨이" },
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = PrimaryColor,
                thickness = 4.dp,
                modifier = Modifier
                    .width(64.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                repeat(3) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = Color(0xFFF79800))
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "이제 ${uiState.petName.ifEmpty { "기쁨이" }} 와 함께\n 특별한 여정을 시작해보세요!",
                fontSize = 24.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate(BottomNavItem.Feed.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("홈으로 가기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate(BottomNavItem.MyPage.route) },
                border = BorderStroke(2.dp, PrimaryColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("마이페이지로 가기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
