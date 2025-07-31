package com.example.petplace.presentation.feature.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.petplace.R

class WalkAndCareViewModel : ViewModel() {

    var searchText by mutableStateOf("")
        private set

    private val allPosts = listOf(
        Post("산책구인", "이 카페 좋으네영", "분위기도 좋고 강아지 간식도 줘요 추천합니다", "인의동 · 4시간 전 · 조회 10", 3, R.drawable.ic_home),
        Post("돌봄구인", "강아지 용품 나눔해요", "새끼 때 쓰던 용품들 필요하신 분께 드려요", "인의동 · 6시간 전 · 조회 25", 7, R.drawable.ic_home),
        Post("산책의뢰", "우리동네 좋은 동물병원 추천", "24시간 응급실 있는 곳으로 알려드려요", "인의동 · 1일 전 · 조회 42", 12, R.drawable.ic_home),
        Post("돌봄의뢰", "우리 댕댕이 첫 산책!", "생후 3개월 처음으로 밖에 나가봤어요", "인의동 · 2일 전 · 조회 67", 18, R.drawable.ic_home),
        Post("돌봄구인", "강아지 사료 공동구매 하실분", "대용량으로 사면 더 저렴해요!", "인의동 · 3일 전 · 조회 31", 9, R.drawable.ic_home)
    )

    var postList by mutableStateOf(allPosts)
        private set

    val tags = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    fun updateSearchText(text: String) {
        searchText = text
    }

    fun applySearch() {
        if (searchText.isBlank()) {
            postList = allPosts
        } else {
            postList = allPosts.filter {
                it.title.contains(searchText, ignoreCase = true)
            }
        }
    }

    fun filterByTag(tag: String) {
        postList = allPosts.filter { it.category == tag }
    }

    fun resetFilter() {
        postList = allPosts
    }
}
