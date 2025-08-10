package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem
import com.example.petplace.presentation.common.theme.PrimaryColor

@Composable
fun ProfileCompleteScreen(
    navController: NavController,
    name: String = "기쁨이",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(32.dp)
    ) {
        Text(
            text = "프로필 등록 완료!",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.TopEnd) {
            Image(
                painter = painterResource(id = R.drawable.outline_sound_detection_dog_barking_24), // 더미 이미지
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )
            Icon(
                painter = painterResource(id = R.drawable.heart), // 하트 아이콘
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 8.dp, y = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(
            color = PrimaryColor,
            thickness = 2.dp,
            modifier = Modifier
                .width(40.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "이제 $name 와 함께 특별한 여정을 시작해보세요!",
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
