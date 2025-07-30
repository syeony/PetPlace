package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BorderColor = Color(0xFFFFEDD5)   // #FFEDD5

@Composable
fun FeatureButton(
    label: String,
    icon: Any,          // ImageVector or Int
    circleColor: Color  // 파스텔톤 배경 원 색
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .border(
                BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이콘 원
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (icon is ImageVector) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    painter = painterResource(icon as Int),
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(label, fontSize = 14.sp, color = Color(0xFF3C3C3C))
    }
}
