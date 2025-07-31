package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem.Chat.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    navController: NavController,
    modifier: Modifier = Modifier               // ← modifier 파라미터 추가
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
            .navigationBarsPadding(), // 내비바 위에서 시작
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }

        }
    ) {
        val buttons = listOf(
            Triple("실종펫 등록",  R.drawable.outline_exclamation_24, Color(0xFFFFC9C5)),
            Triple("실종펫 신고",  R.drawable.outline_search_24,      Color(0xFFD0E4FF)),
            Triple("실종펫 리스트", R.drawable.ic_feed,                Color(0xFFFFE4C1)),
            Triple("돌봄/산책",    R.drawable.outline_sound_detection_dog_barking_24, Color(0xFFCBF4D1)),
            Triple("입양처",       Icons.Default.Favorite,             Color(0xFFFAD3E4)),
            Triple("동물호텔",     R.drawable.outline_home_work_24,   Color(0xFFE6D5FF))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .fillMaxHeight(2f / 3f)
        ) {
            Text(
                text = "우리동네 한눈에 보기",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            buttons.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { (label, iconRes, bgColor) ->
                        val onClick = {
                            if (label == "실종펫 등록") {
                                navController.navigate("Missing_register")
                                onDismiss()
                            }
                            if (label == "실종펫 신고") {
                                navController.navigate("missing_report")
                                onDismiss()
                            }
                        }
                        FeatureButton(
                            label, icon, bgColor, onClick
                        )
                    }
                }
            }
        }
    }
}