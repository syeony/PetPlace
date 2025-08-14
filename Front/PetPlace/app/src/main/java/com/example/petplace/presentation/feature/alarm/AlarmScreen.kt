package com.example.petplace.presentation.feature.alarm

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Text(text = "알람창입니다.", modifier = modifier)
}