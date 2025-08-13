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
import com.example.petplace.data.model.cares.CareItem // ← 아래 3) DTO 정의 참조
import com.example.petplace.data.model.cares.PageResponse
import com.example.petplace.presentation.feature.hotel.ApiResponse


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


// ⚠ 잘못된 임포트 제거: com.google.ai.client.generativeai.type.content (절대 X)

    fun fetchPosts(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            caresRepository.list(page, size)
                .onSuccess { resp ->
                    // resp: Response<ApiResponse<PageResponse<CareItem>>>
                    val body: ApiResponse<PageResponse<CareItem>>? = resp.body()
                    val pageData: PageResponse<CareItem>? = body?.data
                    val cares: List<CareItem> = pageData?.content ?: emptyList()

                    val posts = cares.map { care ->
                        val dateText = formatDateRange(care.startDatetime, care.endDatetime)
                        val timeText = formatTimeRange(care.startDatetime, care.endDatetime)

                        Post(
                            category = care.categoryDescription
                                ?: mapCategoryToKorean(care.category),
                            title = care.title.orEmpty(),
                            // 목록 응답엔 content가 없음 → 대체 텍스트(또는 "")
                            body = buildString {
                                // 필요하면 간단 요약을 조합
                                if (!care.regionName.isNullOrBlank()) append("[${care.regionName}] ")
                                append(timeText.takeIf { it != "-" } ?: dateText)
                            },
                            date = dateText,
                            time = timeText,
                            imageUrl = care.petImg.orEmpty(),
                            reporterName = care.userNickname.orEmpty(),
                            reporterAvatarUrl = care.userImg.orEmpty()
                        )
                    }

                    setPosts(posts)
                }
                .onFailure { it.printStackTrace() }
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
            "CARE_REQ", "CARE_OFFER" -> "돌봄의뢰" // 서버 명칭 중 하나일 수 있어 대비
            else -> "산책구인"
        }

    /** "YYYY-MM-DDTHH:mm[:ss]" → "MM.dd ~ MM.dd" / "MM.dd" */
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

    /** "YYYY-MM-DDTHH:mm[:ss]" → "HH:mm ~ HH:mm" */
    private fun formatTimeRange(start: String?, end: String?): String {
        val st = extractTime(start)
        val et = extractTime(end)
        return when {
            !st.isNullOrBlank() && !et.isNullOrBlank() -> "$st ~ $et"
            !st.isNullOrBlank() -> st
            !et.isNullOrBlank() -> et
            else -> "-"
        }
    }

    private fun extractTime(dt: String?): String? {
        return try {
            if (dt == null) return null
            val tPart = dt.split('T', ' ').getOrNull(1) ?: return null
            tPart.substring(0, 5) // HH:mm
        } catch (_: Exception) { null }
    }
}
