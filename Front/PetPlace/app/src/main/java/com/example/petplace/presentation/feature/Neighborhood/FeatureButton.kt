package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    icon: Any,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier // ✅ 전달받은 modifier부터 시작
            .border(
                BorderStroke(1.dp, Color(0xFFFFC981)),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon is ImageVector) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Icon(
                painter = painterResource(icon as Int),
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(label, fontSize = 14.sp, color = Color(0xFF3C3C3C))
    }
}

