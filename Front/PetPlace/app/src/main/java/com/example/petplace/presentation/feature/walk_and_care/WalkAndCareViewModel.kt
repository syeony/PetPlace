package com.example.petplace.presentation.feature.walk_and_care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.data.model.cares.CareItem
import com.example.petplace.data.model.cares.PageResponse
import com.example.petplace.data.repository.CaresRepository
import com.example.petplace.presentation.feature.hotel.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkAndCareViewModel @Inject constructor(
    private val caresRepository: CaresRepository
) : ViewModel() {

    // 검색어
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // 선택된 카테고리: null 이면 "전체" 취급
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // 전체 포스트 (서버 원본)
    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())
    val allPosts: StateFlow<List<Post>> = _allPosts.asStateFlow()

    // 탭에서 보여줄 카테고리 (전체 제거)
    val allCategories: List<String> = listOf("산책구인", "돌봄구인", "산책의뢰", "돌봄의뢰")

    // 필터 결과: 카테고리(null=전체) + 검색어 반영
    val filteredPosts: StateFlow<List<Post>> =
        combine(_allPosts, _selectedCategory, _searchText) { list, selected, query ->
            val q = query.trim()

            list.filter { p ->
                val matchesCategory = selected == null ||
                        normalizeCategoryLabel(p.category) == normalizeCategoryLabel(selected)

                val matchesQuery = q.isBlank() ||
                        p.title.contains(q, ignoreCase = true) ||
                        p.body.contains(q, ignoreCase = true)

                matchesCategory && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        fetchPosts()
    }

    // ───────── Actions ─────────
    fun toggleCategory(cat: String) {
        // 같은 걸 한 번 더 누르면 해제(null) → 전체 취급
        _selectedCategory.value = if (_selectedCategory.value == cat) null else cat
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun clearFilters() {
        _selectedCategory.value = null   // 전체
        _searchText.value = ""
    }

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
                            category = normalizeCategoryLabel(
                                care.categoryDescription ?: mapEnumToKoreanLabel(care.category)
                            ),
                            title = care.title.orEmpty(),
                            body = buildString {
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

                    _allPosts.value = posts
                }
                .onFailure { it.printStackTrace() }
        }
    }

    private fun normalizeCategoryLabel(src: String?): String {
        val k = src?.trim()?.replace(" ", "")?.uppercase() ?: return "산책구인"
        return when (k) {
            "산책구인", "산책구해요", "WALK_WANT" -> "산책구인"
            "돌봄구인", "돌봄구해요", "CARE_WANT" -> "돌봄구인"
            "산책의뢰", "산책해줄게요", "WALK_OFFER" -> "산책의뢰"
            "돌봄의뢰", "돌봄해줄게요", "CARE_OFFER", "CARE_REQ" -> "돌봄의뢰"
            else -> "산책구인"
        }
    }

    private fun mapEnumToKoreanLabel(enumName: String?): String =
        when (enumName?.uppercase()) {
            "WALK_WANT"  -> "산책구인"
            "CARE_WANT"  -> "돌봄구인"
            "WALK_OFFER" -> "산책의뢰"
            "CARE_OFFER", "CARE_REQ" -> "돌봄의뢰"
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
            val tPart = dt?.split('T', ' ')?.getOrNull(1) ?: return null
            tPart.substring(0, 5) // HH:mm
        } catch (_: Exception) {
            null
        }
    }
}
