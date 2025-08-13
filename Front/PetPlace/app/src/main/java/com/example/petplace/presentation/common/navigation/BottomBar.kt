package com.example.petplace.presentation.common.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.petplace.presentation.common.theme.PrimaryColor

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Feed,
        BottomNavItem.Neighborhood,
        BottomNavItem.CreateFeed,
        BottomNavItem.Chat,
        BottomNavItem.MyPage
    )
    val currentBackStack = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStack?.destination?.route

    NavigationBar(
        //containerColor = BackgroundSoft,
        containerColor = Color.White,              // ① 배경을 흰색으로
        modifier = Modifier.navigationBarsPadding() // 여기를 꼭 추가


    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(painterResource(item.icon), contentDescription = null) },
//                label = { Text(stringResource(item.title)) },
                interactionSource = remember { MutableInteractionSource() }, // ripple 영향 최소화
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = PrimaryColor,
                    selectedTextColor = PrimaryColor,
                    unselectedIconColor = Color(0xFF9CA3AF),   // ② 비활성 상태 색
                    unselectedTextColor = Color(0xFF9CA3AF)
                )
            )

        }
    }
}
