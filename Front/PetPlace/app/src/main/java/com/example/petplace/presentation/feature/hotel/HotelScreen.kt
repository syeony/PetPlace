package com.example.petplace.presentation.feature.hotel

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun HotelScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Text(text = "동물호텔 화면입니다.", modifier = modifier)
}