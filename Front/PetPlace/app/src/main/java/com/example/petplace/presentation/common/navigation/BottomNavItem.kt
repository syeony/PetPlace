package com.example.petplace.presentation.common.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.petplace.R

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    object Feed : BottomNavItem("nav_feed", R.drawable.ic_feed, R.string.feed)
    object Neighborhood : BottomNavItem("nav_neighborhood", R.drawable.ic_map, R.string.neighborhood)
    object Chat : BottomNavItem("nav_chat", R.drawable.ic_chat, R.string.chat)
    object MyPage : BottomNavItem("nav_mypage", R.drawable.ic_mypage, R.string.mypage)
    //홈  게시판 지도 채팅 마이페이지
    // HotelRoutes.kt

    object HotelRoutes {
        const val Graph = "hotelGraph"
        const val Hotel = "hotel"
        const val DateSelection = "dateSelection"
        const val HotelList = "hotelList"
    }

}
