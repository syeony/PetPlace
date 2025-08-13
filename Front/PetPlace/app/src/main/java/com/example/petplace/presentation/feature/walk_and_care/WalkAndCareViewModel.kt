package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.data.repository.CaresRepository
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkAndCareViewModel @Inject constructor(
    private val caresRepository: CaresRepository
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    val allCategories = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    private var allPosts: List<Post> = emptyList()

    init {
        fetchPosts()
    }

    /** 서버에서 데이터 불러오기 */
    fun fetchPosts(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            caresRepository.list(page, size)
                .onSuccess { resp ->
                    val cares = resp.body()?.data ?: emptyList() // List<CareSummary>

                    val posts = cares.map { care ->
                        val dateText = formatDateRange(care.startDatetime, care.endDatetime)
                        val timeText = formatTimeRange(care.startDatetime, care.endDatetime)

                        Post(
                            category = care.categoryDescription
                                ?: mapCategoryToKorean(care.category?.name),
                            title = care.title,
                            body = care.content,
                            date = dateText,
                            time = timeText,
                            imageUrl = care.petImg ?: "", // 없으면 첫 이미지 리스트 필드 쓰세요
                            reporterName = care.userNickname ?: "",
                            reporterAvatarUrl = care.userImg ?: ""
                        )
                    }

                    setPosts(posts)
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
        }
    }

    fun toggleCategory(cat: String) {
        _selectedCategory.update { if (it == cat) null else cat }
        applyFilters()
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
        applyFilters()
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _searchText.value = ""
        applyFilters()
    }

    fun setPosts(posts: List<Post>) {
        allPosts = posts
        applyFilters()
    }

    private fun applyFilters() {
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

    // --------- helpers ---------

    private fun mapCategoryToKorean(enumName: String?): String =
        when (enumName) {
            "WALK_WANT"  -> "산책구인"
            "CARE_WANT"  -> "돌봄구인"
            "WALK_OFFER" -> "산책의뢰"
            "CARE_OFFER" -> "돌봄의뢰"
            else -> "산책구인" // 기본값(원하면 빈 문자열로 변경)
        }

    /** "YYYY-MM-DDTHH:mm[:ss]" 형태를 "MM.dd ~ MM.dd" 또는 "MM.dd"로 */
    private fun formatDateRange(start: String?, end: String?): String {
        val s = start?.takeIf { it.length >= 10 }?.substring(5, 10)?.replace("-", ".")
        val e = end?.takeIf { it.length >= 10 }?.substring(5, 10)?.replace("-", ".")
        return when {
            !s.isNullOrBlank() && !e.isNullOrBlank() -> "$s ~ $e"
            !s.isNullOrBlank() -> s
            !e.isNullOrBlank() -> e
            else -> "-"
        }
    }

    /** "YYYY-MM-DDTHH:mm[:ss]" 형태를 "HH:mm ~ HH:mm" (둘 다 있으면) */
    private fun formatTimeRange(start: String?, end: String?): String {
        val st = start?.let { extractTime(it) }
        val et = end?.let { extractTime(it) }
        return when {
            !st.isNullOrBlank() && !et.isNullOrBlank() -> "$st ~ $et"
            !st.isNullOrBlank() -> st
            !et.isNullOrBlank() -> et
            else -> "-"
        }
    }

    private fun extractTime(dt: String): String? {
        return try {
            // "YYYY-MM-DDTHH:mm:ss" 또는 "YYYY-MM-DD HH:mm" 등에서 HH:mm 추출
            val tPart = dt.split('T', ' ').getOrNull(1) ?: return null
            tPart.substring(0, 5) // HH:mm
        } catch (_: Exception) { null }
    }
}
