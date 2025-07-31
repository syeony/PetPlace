package com.example.petplace.presentation.feature.missing_list

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun MissingListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Text(text = "실종펫 리스트 화면입니다.", modifier = modifier)
}