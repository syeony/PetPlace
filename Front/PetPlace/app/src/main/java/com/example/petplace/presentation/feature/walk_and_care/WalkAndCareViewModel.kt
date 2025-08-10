package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.Walk.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalkAndCareViewModel : ViewModel() {

    // 검색어
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    // 카테고리(한 개 선택)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    // 화면에 보여줄 필터 결과
    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    // 카테고리 탭
    val allCategories = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    // 더미 데이터 (API 나오면 setPosts로 교체)
    // WalkAndCareViewModel.kt (더미 데이터 부분만 교체)
    private var allPosts: List<Post> = listOf(
        Post(
            category = "산책구인",
            title    = "우리 댕댕이 산책시켜주실 분 구합니다!",
            body     = "순하고 안 물어요. 사람 잘 따라요. 뼈간식을 좋아해요, 조금 드릴게요. 2시간 산책이 필요해요.",
            date = "07.25",
            time="13:00 ~ 15:00",
            imageUrl = "https://images.unsplash.com/photo-1568572933382-74d440642117" // 🐶 산책
        ),
        Post(
            category = "돌봄구인",
            title    = "주말 돌봄 부탁드려요",
            body     = "10kg 믹스견, 분리불안 없음. 사료/간식 제공, 근처 공원 30분 산책만 부탁드려요. CCTV 있어요.",
            date = "07.25 ~ 07.26",
            time="-",
            imageUrl = "https://images.unsplash.com/photo-1568572933382-74d440642117" // 🐶 돌봄
        ),
        Post(
            category = "산책의뢰",
            title    = "저도 강아지 산책시키고 싶어요",
            body     = "강아지 용품 인증 다 해놨어요 제 프로필에 들어오셔서 확인하세요! 누구보다 강아지를 좋아합니다!",
            date = "07.25",
            time="13:00 ~ 15:00",
            imageUrl = "https://images.unsplash.com/photo-1568572933382-74d440642117" // ☔ 실내 산책
        ),
        Post(
            category = "돌봄의뢰",
            title    = "오늘부터 일주일간 봐드릴 수 있어요",
            body     = "와이프 출장가서 일주일동안 강아지 돌보아줄 수 있습니다. 연락주세요! 아들도 좋아합니다.",
            date = "07.25 ~ 07.31",
            time="-",
            imageUrl = "https://images.unsplash.com/photo-1568572933382-74d440642117" // 🏠 위탁
        ),
        Post(
            category = "돌봄구인",
            title    = "일주일 케어 구합니다",
            body     = "간단한 놀이 + 급수 교체, 간식 급여 부탁드립니다. 조류라서 돌보기 까다롭진 않으실거에요.",
            date = "07.25 ~ 07.31",
            time="-",
            imageUrl = "https://images.unsplash.com/photo-1589923188900-85dae523342b" // 🌙 저녁 돌봄
        )
    )

    init {
        applyFilters()
    }

    /** 카테고리 토글 (같은 것 다시 누르면 해제) */
    fun toggleCategory(cat: String) {
        _selectedCategory.update { if (it == cat) null else cat }
        applyFilters()
    }

    /** 검색어 변경 */
    fun updateSearchText(text: String) {
        _searchText.value = text
        applyFilters()
    }

    /** 필터 초기화 */
    fun clearFilters() {
        _selectedCategory.value = null
        _searchText.value = ""
        applyFilters()
    }

    /** (API 붙일 때 사용) 서버 데이터로 교체 */
    fun setPosts(posts: List<Post>) {
        allPosts = posts
        applyFilters()
    }

    /** 실제 필터링 로직 */
    private fun applyFilters() = viewModelScope.launch {
        val cat = _selectedCategory.value
        val q = _searchText.value.trim().lowercase()

        _filteredPosts.value = allPosts.filter { post ->
            val hitCat = (cat == null || post.category == cat)
            val hitQuery = q.isBlank() ||
                    post.title.lowercase().contains(q) ||
                    post.body.lowercase().contains(q)
            hitCat && hitQuery
        }
    }
}
