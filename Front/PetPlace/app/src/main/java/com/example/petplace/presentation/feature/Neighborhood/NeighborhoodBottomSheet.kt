package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petplace.R

/**
 * 우리동네 바텀시트 UI
 * @param onDismiss 바텀시트 닫힘 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    navController: NavController
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White
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

            // 버튼 구성
            buttons.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { (label, icon, bgColor) ->
                        val onClick = {
                            if (label == "실종펫 등록") {
                                navController.navigate("Missing_register")
                                onDismiss()             // 시트 닫기
                            }
                            if (label == "실종펫 신고") {
                                navController.navigate("missing_report")
                                onDismiss()
                            }
                        }
                        FeatureButton(label, icon, bgColor, onClick)
                    }
                }
            }
        }
    }
}
