package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.R
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.data.local.feed.FeedDto
import com.example.petplace.presentation.feature.feed.dummyFeeds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalkAndCareViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _allPosts = listOf(
        Post("산책구인", "이 카페 좋으네영", "분위기도 좋고 강아지 간식도 줘요 추천합니다", "인의동 · 4시간 전 · 조회 10", 3, R.drawable.pp_logo),
        Post("돌봄구인", "강아지 용품 나눔해요", "새끼 때 쓰던 용품들 필요하신 분께 드려요", "인의동 · 6시간 전 · 조회 25", 7, R.drawable.pp_logo),
        Post("산책의뢰", "우리동네 좋은 동물병원 추천", "24시간 응급실 있는 곳으로 알려드려요", "인의동 · 1일 전 · 조회 42", 12, R.drawable.pp_logo),
        Post("돌봄의뢰", "우리 댕댕이 첫 산책!", "생후 3개월 처음으로 밖에 나가봤어요", "인의동 · 2일 전 · 조회 67", 18, R.drawable.pp_logo),
        Post("돌봄구인", "강아지 사료 공동구매 하실분", "대용량으로 사면 더 저렴해요!", "인의동 · 3일 전 · 조회 31", 9, R.drawable.pp_logo)
    )

    /* 더미 피드 데이터 */
    private val _allFeeds = MutableStateFlow(dummyFeeds)
    private val _filteredFeeds = MutableStateFlow(dummyFeeds)
    val filteredFeeds: StateFlow<List<FeedDto>> = _filteredFeeds

    private val _filteredPosts = MutableStateFlow(_allPosts)
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts


    val allCategories = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    /* 하나만 선택(재클릭 → 해제) */
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init { applyFilters() }

    /* ------------ 카테고리 토글 ------------ */
    fun toggleCategory(cat: String) {
        _selectedCategory.update { if (it == cat) null else cat }
        applyFilters()
    }

    /* ------------ 검색어 ------------ */
    fun updateSearchText(t: String) {
        _searchText.value = t
        applyFilters()
    }

    /* ------------ 필터링 ------------ */
    private fun applyFilters() = viewModelScope.launch {
        val cat = _selectedCategory.value
        val query = _searchText.value.lowercase()

        _filteredFeeds.value = _allFeeds.value.filter { feed ->
            (cat == null || feed.category == cat) &&
                    (query.isBlank() || feed.content.lowercase().contains(query))
        }

        _filteredPosts.value = _allPosts.filter { post ->
            (cat == null || post.category == cat) &&
                    (query.isBlank() || post.title.lowercase().contains(query) || post.title.lowercase().contains(query))
        }
    }
}
