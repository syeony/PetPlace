package com.example.petplace.presentation.common.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    background = BackgroundColor,
    onBackground = TextColor,
    surface = BackgroundSoft,
    onSurface = TextPrimary
)

@Composable
fun PetPlaceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography, // Typography.kt에 정의되어 있어야 함
        content = content
    )
}
