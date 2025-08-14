package com.example.petplace.presentation.common.navigation

import androidx.annotation.DrawableRes
import com.example.petplace.R

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val icon: Int,
//    @StringRes val title: Int
) {
    object Feed : BottomNavItem("nav_feed", R.drawable.ic_feed)
    object Neighborhood : BottomNavItem("nav_neighborhood", R.drawable.ic_map)
    object CreateFeed: BottomNavItem("nav_createfeed", R.drawable.outline_add_box_24)
    object Chat : BottomNavItem("nav_chat", R.drawable.outline_chat_bubble_24) //ic_chat
    object MyPage : BottomNavItem("nav_mypage", R.drawable.ic_mypage)
    //홈  게시판 지도 채팅 마이페이지
    // HotelRoutes.kt

    object HotelRoutes {
        const val Graph = "hotelGraph"
        const val Hotel = "hotel"
        const val DateSelection = "dateSelection"
        const val HotelList = "hotelList"
    }

}
