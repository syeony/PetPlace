package com.example.petplace.presentation.feature.mypage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.petplace.PetPlaceApp
import com.example.petplace.presentation.common.theme.AppTypography
import com.example.petplace.presentation.common.theme.PrimaryColor

@Composable
fun MyPageScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    Button(
        onClick = {
            app.clearLoginData()
            navController.navigate("login")
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("로그아웃", style = AppTypography.labelLarge)
    }
}
