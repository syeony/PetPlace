package com.example.petplace.presentation.common.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.petplace.R

sealed class BottomNavItem(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    object Home : BottomNavItem("nav_home", R.drawable.ic_home, R.string.home)
    object Board : BottomNavItem("nav_board", R.drawable.ic_board, R.string.board)
    object Map : BottomNavItem("nav_map", R.drawable.ic_map, R.string.map)
    object Chat : BottomNavItem("nav_chat", R.drawable.ic_chat, R.string.chat)
    object MyPage : BottomNavItem("nav_mypage", R.drawable.ic_mypage, R.string.mypage)
    //홈  게시판 지도 채팅 마이페이지
}
