package com.example.petplace.presentation.feature.mypage

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.local.Walk.Post
import com.example.petplace.data.model.cares.CareItem
import com.example.petplace.data.model.cares.PageResponse
import com.example.petplace.data.remote.UserApiService
import com.example.petplace.data.repository.CaresRepository
import com.example.petplace.presentation.feature.hotel.ApiResponse
import com.example.petplace.presentation.feature.walk_and_care.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyWalkAndCareViewModel @Inject constructor(
    private val caresRepository: CaresRepository,
//    private val userApi: UserApiService,
//    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _walkPosts = MutableStateFlow<List<Post>>(emptyList())
    val walkPosts: StateFlow<List<Post>> = _walkPosts.asStateFlow()

    private val _carePosts = MutableStateFlow<List<Post>>(emptyList())
    val carePosts: StateFlow<List<Post>> = _carePosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
//        // 앱 시작 시 자동으로 위치 가져오기
//        loadCurrentLocationAndSetRegion()

//        viewModelScope.launch {
//            regionId.collect { id ->
//                if (id != null) {
//                    fetchMyWalkPosts()
//                    fetchMyCarePosts()
//                }
//            }
//        }
    }

//    fun loadCurrentLocationAndSetRegion() {
//        viewModelScope.launch {
//            // 권한 체크
//            val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
//            val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
//            val hasFine = ContextCompat.checkSelfPermission(context, fine) ==
//                    android.content.pm.PackageManager.PERMISSION_GRANTED
//            val hasCoarse = ContextCompat.checkSelfPermission(context, coarse) ==
//                    android.content.pm.PackageManager.PERMISSION_GRANTED
//
//            if (hasFine || hasCoarse) {
//                try {
//                    val location = LocationProvider.getCurrentLocation(context)
//                    if (location != null) {
//                        Log.d("MyWalkAndCareVM", "GPS lat=${location.latitude}, lon=${location.longitude}")
//                        setRegionByLocation(location.latitude, location.longitude)
//                    } else {
//                        Log.e("MyWalkAndCareVM", "현재 위치를 가져오지 못했습니다 (null)")
//                        _error.value = "위치 정보를 가져올 수 없습니다"
//                    }
//                } catch (e: Exception) {
//                    Log.e("MyWalkAndCareVM", "위치 가져오기 실패", e)
//                    _error.value = "위치 정보를 가져오는 중 오류가 발생했습니다"
//                }
//            } else {
//                _error.value = "위치 권한이 필요합니다"
//            }
//        }
//    }

//    /**
//     * 위치 정보를 기반으로 지역 인증을 수행합니다.
//     */
//    fun setRegionByLocation(lat: Double, lon: Double) {
//        Log.d("MyWalkAndCareVM", "위도=$lat, 경도=$lon")
//        viewModelScope.launch {
//            val res = runCatching { userApi.authenticateDong(lat, lon) }
//                .onFailure { e ->
//                    Log.e("MyWalkAndCareVM", "dong-auth API fail", e)
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
//            Log.d("MyWalkAndCareVM", "regionName(final)=$finalName, regionId(final)=$finalId")
//
//            _regionName.value = " $finalName"
//            _regionId.value = finalId
//        }
//    }

    /**
     * 내가 작성한 산책 게시글만 가져옵니다.
     */
    fun fetchMyWalkPosts() {
        viewModelScope.launch {
//            val id = regionId
//            if (id == null) {
//                Log.e("MyWalkAndCareVM", "regionId 없음 → fetchMyWalkPosts 중단")
//                _error.value = "지역 정보를 먼저 설정해주세요"
//                return@launch
//            }

            _isLoading.value = true
            _error.value = null

            try {
                caresRepository.myList(page = 0, size = 100)
                    .onSuccess { resp ->
                        val body: ApiResponse<PageResponse<CareItem>>? = resp.body()
                        val pageData: PageResponse<CareItem>? = body?.data
                        val cares: List<CareItem> = pageData?.content ?: emptyList()

                        val allPosts = cares.map { care -> convertCareToPost(care) }

                        // 산책 관련 게시글만 필터링 (산책구인, 산책의뢰)
                        val walkPosts = allPosts.filter {
                            it.category == "산책구인" || it.category == "산책의뢰"
                        }

                        _walkPosts.value = walkPosts
                        Log.d("MyWalkAndCareVM", "내 산책 게시글: ${walkPosts.size}개")
                    }
                    .onFailure { e ->
                        Log.e("MyWalkAndCareVM", "산책 게시글 조회 실패", e)
                        _error.value = e.message ?: "산책 게시글을 불러올 수 없습니다"
                    }
            } catch (e: Exception) {
                Log.e("MyWalkAndCareVM", "산책 게시글 조회 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 내가 작성한 돌봄 게시글만 가져옵니다.
     */
    fun fetchMyCarePosts() {
        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {
                caresRepository.myList(page = 0, size = 100)
                    .onSuccess { resp ->
                        val body: ApiResponse<PageResponse<CareItem>>? = resp.body()
                        val pageData: PageResponse<CareItem>? = body?.data
                        val cares: List<CareItem> = pageData?.content ?: emptyList()

                        val allPosts = cares.map { care -> convertCareToPost(care) }

                        // 돌봄 관련 게시글만 필터링 (돌봄구인, 돌봄의뢰)
                        val carePosts = allPosts.filter {
                            it.category == "돌봄구인" || it.category == "돌봄의뢰"
                        }

                        _carePosts.value = carePosts
                        Log.d("MyWalkAndCareVM", "내 돌봄 게시글: ${carePosts.size}개")
                    }
                    .onFailure { e ->
                        Log.e("MyWalkAndCareVM", "돌봄 게시글 조회 실패", e)
                        _error.value = e.message ?: "돌봄 게시글을 불러올 수 없습니다"
                    }
            } catch (e: Exception) {
                Log.e("MyWalkAndCareVM", "돌봄 게시글 조회 예외 발생", e)
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 지역 정보를 초기화하고 다시 설정합니다.
     */
//    fun refreshRegion(lat: Double, lon: Double) {
//        _regionName.value = null
//        _regionId.value = null
//        setRegionByLocation(lat, lon)
//    }

    /**
     * CareItem을 Post로 변환하는 공통 함수
     */
    private fun convertCareToPost(care: CareItem): Post {
        val dateText = formatDateRange(care.startDatetime, care.endDatetime)
        val timeText = formatTimeRange(care.startDatetime, care.endDatetime)

        val allImages: List<String> =
            care.images.sortedBy { it.sort }
                .mapNotNull { it.src?.trim() }
                .filter { it.isNotBlank() }

        return Post(
            id = care.id,
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

    /**
     * 오류 상태를 초기화합니다.
     */
    fun clearError() {
        _error.value = null
    }

    // ---------- enum → 화면 라벨 ----------
    private fun displayLabelFromCategoryEnum(enumName: String?): String =
        when (enumName?.uppercase()) {
            "WALK_WANT" -> "산책구인"
            "WALK_REQ"  -> "산책의뢰"
            "CARE_WANT" -> "돌봄구인"
            "CARE_REQ"  -> "돌봄의뢰"
            else        -> "산책구인"
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
        } catch (_: Exception) { null }
    }

    // 카드 썸네일 선택
    private fun pickPreviewImage(care: CareItem): String {
        val primary = care.images.firstOrNull { it.sort == 0 }?.src
        if (!primary.isNullOrBlank()) return primary
        val fallback = care.images.minByOrNull { it.sort }?.src
        return fallback?.trim().orEmpty()
    }
}