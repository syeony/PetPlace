package com.example.petplace.presentation.common.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    background = BackgroundWhite,
    onBackground = TextColor,
    surface = BackgroundWhite,
    onSurface = TextPrimary,

    // 있으면 함께 지정 (버전에 따라 일부 키 미제공)
    surfaceVariant = BackgroundWhite,
    onSurfaceVariant = TextPrimary,

    // 1.2+ 에서 제공되는 surface container 계열
    surfaceContainerLowest = BackgroundWhite,
    surfaceContainerLow = BackgroundWhite,
    surfaceContainer = BackgroundWhite,
    surfaceContainerHigh = BackgroundWhite,
    surfaceContainerHighest = BackgroundWhite,

    // tint 자체를 안 쓰고 싶다면(가능한 버전에서만)
    surfaceTint = Color.Unspecified // or Color.Transparent
)
@Composable
fun PetPlaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors, // 너가 올린 LightColors (surface/background 전부 White)
        typography = AppTypography
    ) {
        // 🔑 전역으로 톤 오버레이 제거 → 바텀바/서피스 회색기 사라짐
        CompositionLocalProvider(
            androidx.compose.material3.LocalAbsoluteTonalElevation provides 0.dp
        ) {
            // 루트 배경도 테마값으로 고정
            Surface(color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}
