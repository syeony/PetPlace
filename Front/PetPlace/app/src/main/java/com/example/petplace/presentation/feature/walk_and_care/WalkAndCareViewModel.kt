package com.example.petplace.presentation.feature.walk_and_care

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.data.model.cares.CareItem
import com.example.petplace.data.model.cares.PageResponse
import com.example.petplace.data.remote.UserApiService
import com.example.petplace.data.repository.CaresRepository
import com.example.petplace.data.repository.MyPageRepository
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
    private val caresRepository: CaresRepository,
    private val myPageRepository: MyPageRepository,
) : ViewModel() {

    val app = PetPlaceApp.getAppContext() as PetPlaceApp
    val userInfo = app.getUserInfo()

    private val regionId = userInfo?.regionId ?: 0

    private val _regionName = MutableStateFlow<String?>(null)
    val regionName: StateFlow<String?> = _regionName.asStateFlow()

    init {
        viewModelScope.launch {
            myPageRepository.getMyPageInfo()
                .onSuccess { response ->
                    _regionName.value = response.regionName
                }
                .onFailure {
                    it.printStackTrace()
                }
        }
        fetchPosts()
    }

//    fun setRegionByLocation(lat: Double, lon: Double) {
//        Log.d("WalkVM", "위도=$lat, 경도=$lon")
//        viewModelScope.launch {
//            val res = runCatching { userApi.authenticateDong(lat, lon) }
//                .onFailure { e ->
//                    Log.e("WalkVM", "dong-auth API fail", e)
//                    _regionName.value = " 동네 인증 실패"
//                }
//                .getOrNull() ?: return@launch
//
//            if (!res.success) {
//                _regionName.value = " 동네 인증 실패"
//                return@launch
//            }
//
//            val finalName = res.data?.regionName?.trim().orEmpty()
//            val finalId = res.data?.regionId
//
//            Log.d("WalkVM", "regionName(final)=$finalName, regionId(final)=$finalId")
//
//            _regionName.value = " $finalName"
//            regionId = finalId
//
//            if (finalId != null) {
//                fetchPosts(page = 0, size = 20)
//            }
//        }
//    }

    // ───────── 검색/필터 상태 ─────────
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())

    // UI 버튼용 고정 라벨 (enum → 라벨 매핑 결과)
    val allCategories: List<String> = listOf("산책구인", "산책의뢰", "돌봄구인", "돌봄의뢰")

    val filteredPosts: StateFlow<List<Post>> =
        combine(_allPosts, _selectedCategory, _searchText) { list, selected, query ->
            val q = query.trim()
            list.filter { p ->
                val matchesCategory = selected == null || p.category == selected
                val matchesQuery = q.isBlank() ||
                        p.title.contains(q, ignoreCase = true) ||
                        p.body.contains(q, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    fun toggleCategory(cat: String) {
        _selectedCategory.value = if (_selectedCategory.value == cat) null else cat
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun clearFilters() {
        _selectedCategory.value = null; _searchText.value = ""
    }

    fun fetchPosts(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            val id = regionId
            if (id == null) {
                Log.e("WalkVM", "regionId 없음 → fetchPosts 중단")
                return@launch
            }
            caresRepository.list(page, size, regionId = id)
                .onSuccess { resp ->
                    Log.d("resp", "fetchPosts: ${resp}")
                    val body: ApiResponse<PageResponse<CareItem>>? = resp.body()
                    val pageData: PageResponse<CareItem>? = body?.data
                    val cares: List<CareItem> = pageData?.content ?: emptyList()

                    val posts = cares.map { care ->
                        val dateText = formatDateRange(care.startDatetime, care.endDatetime)
                        val timeText = formatTimeRange(care.startDatetime, care.endDatetime)

                        val allImages: List<String> =
                            care.images.sortedBy { it.sort }
                                .mapNotNull { it.src?.trim() }
                                .filter { it.isNotBlank() }

                        Post(
                            id = care.id,
                            // ✅ enum 기준으로만 라벨 생성
                            category = displayLabelFromCategoryEnum(care.category),
                            title = care.title.orEmpty(),
                            body = care.content.orEmpty(),
                            date = dateText,
                            time = timeText,
                            imageUrl = pickPreviewImage(care),
                            images = allImages,
                            reporterName = care.userNickname.orEmpty(),
                            reporterAvatarUrl = care.userImg
                        )
                    }
                    _allPosts.value = posts
                }
                .onFailure { it.printStackTrace() }
        }
    }

    // ---------- enum → 화면 라벨 ----------
    private fun displayLabelFromCategoryEnum(enumName: String?): String =
        when (enumName?.uppercase()) {
            "WALK_WANT" -> "산책구인"
            "WALK_REQ" -> "산책의뢰"
            "CARE_WANT" -> "돌봄구인"
            "CARE_REQ" -> "돌봄의뢰"
            else -> "산책구인"
        }

    // ---------- 날짜/시간 포맷 ----------
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
            tPart.substring(0, 5)
        } catch (_: Exception) {
            null
        }
    }
}

// 카드 썸네일 선택
private fun pickPreviewImage(care: CareItem): String {
    val primary = care.images.firstOrNull { it.sort == 0 }?.src
    if (!primary.isNullOrBlank()) return primary
    val fallback = care.images.minByOrNull { it.sort }?.src
    return fallback?.trim().orEmpty()
}
