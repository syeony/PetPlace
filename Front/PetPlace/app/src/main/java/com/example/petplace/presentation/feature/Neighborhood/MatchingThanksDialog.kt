package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.R

private val AccentOrange = Color(0xFFF79800)   // #F79800

@Composable
fun MatchingThanksDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick  = onDismiss,
                colors   = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("확인", color = Color.White)
            }
        },
        shape  = RoundedCornerShape(16.dp),
        title  = {},                              // 제목 공간 비우기
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                /*── 아이콘만 단독으로, 더 크게 ──*/
                Icon(
                    painter = painterResource(R.drawable.notification),
                    contentDescription = null,
                    tint = Color.Unspecified,     // PNG 본연의 색상 유지
                    modifier = Modifier.size(56.dp)   // 원하는 만큼 확대 (예: 56dp)
                )

                Spacer(Modifier.height(24.dp))
                Text("감사합니다!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "매칭이 되면\n주인에게 알람이 갑니다!",
                    fontSize   = 14.sp,
                    lineHeight = 20.sp,
                    color      = Color.Gray,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
            }
        },
        containerColor = Color.White
    )
}
