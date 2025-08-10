package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.R
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
            meta     = "인의동 · 4시간 전 · 조회 10",
            imageRes = R.drawable.pp_logo
        ),
        Post(
            category = "돌봄구인",
            title    = "주말 낮 돌봄 부탁드려요",
            body     = "10kg 믹스견, 분리불안 없음. 사료/간식 제공, 근처 공원 30분 산책만 부탁드려요. CCTV 있어요.",
            meta     = "인의동 · 6시간 전 · 조회 25",
            imageRes = R.drawable.pp_logo
        ),
        Post(
            category = "산책의뢰",
            title    = "비 오는 날 실내 산책 코스 추천 구해요",
            body     = "미끄럽지 않고 애견 동반 가능한 실내/실외 코스 추천 부탁드립니다. 주차 가능하면 더 좋아요.",
            meta     = "인의동 · 1일 전 · 조회 42",
            imageRes = R.drawable.pp_logo
        ),
        Post(
            category = "돌봄의뢰",
            title    = "장기 출장 동안 위탁 가능한 곳 있을까요?",
            body     = "2주 위탁 예정. 예방접종 완료, 사람/강아지 모두 친화적. 배변패드 사용 잘합니다.",
            meta     = "인의동 · 2일 전 · 조회 67",
            imageRes = R.drawable.pp_logo
        ),
        Post(
            category = "돌봄구인",
            title    = "평일 저녁 2시간 케어 구합니다",
            body     = "직장 때문에 귀가가 늦어요. 급여 협의, 간단한 놀이 + 급수 교체, 간식 급여 부탁드립니다.",
            meta     = "인의동 · 3일 전 · 조회 31",
            imageRes = R.drawable.pp_logo
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
