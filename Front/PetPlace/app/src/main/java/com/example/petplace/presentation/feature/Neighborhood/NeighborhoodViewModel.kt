package com.example.petplace.presentation.feature.Neighborhood

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.R
import com.example.petplace.data.repository.KakaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/* 1️⃣ 태그용 데이터 클래스 */
data class TagItem(
    val iconRes: Int,   // drawable 에 있는 PNG
    val label:   String // 화면에 보일 글자
)

@HiltViewModel
class NeighborhoodViewModel @Inject constructor(
    private val kakaoRepository: KakaoRepository
) : ViewModel() {

    /* --- 고정 데이터 --- */
//    val tags = listOf("#식당", "#카페", "#병원", "#용품샵", "#동물병원")

    /* 2️⃣ 리스트 생성 */
    val tags = listOf(
        TagItem(R.drawable.dinner, "애견동반식당"),
        TagItem(R.drawable.coffee,       "애견동반카페"),
        TagItem(R.drawable.hospital,   "동물병원"),
        TagItem(R.drawable.ball,       "반려동물용품샵"),
        TagItem(R.drawable.hotel,        "동물호텔")
    )
    /* --- UI 상태 --- */
    private val _selectedTag = MutableStateFlow<TagItem?>(null)
    val selectedTag = _selectedTag.asStateFlow()
    fun selectTag(tag: TagItem) { _selectedTag.value = tag }

    private val _showBottomSheet = MutableStateFlow(true)
    val showBottomSheet = _showBottomSheet.asStateFlow()

    private val _showThanksDialog = MutableStateFlow(false)
    val showThanksDialog = _showThanksDialog.asStateFlow()

    // 지도 마커 좌표 목록 (lat, lng)
    private val _markers = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val markers = _markers.asStateFlow()

    /* --- 상태 변경 함수 --- */
    fun hideBottomSheet() { _showBottomSheet.value = false }
    fun setThanksDialog(visible: Boolean) { _showThanksDialog.value = visible }

    // 입양처 넘어가기 전 다이얼로그창 띄우기
    private val _showAdoptConfirm = MutableStateFlow(false)
    val showAdoptConfirm: StateFlow<Boolean> = _showAdoptConfirm

    fun setShowAdoptConfirm(value: Boolean) {
        _showAdoptConfirm.value = value
    }

    fun searchPlaces(keyword: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                var safeKeyword = keyword.replace("#", "")

                val places = kakaoRepository.searchPlaces(
                    keyword = safeKeyword,
                    x = lng,
                    y = lat,
                    radius = 10000
                )



                // x=경도(lng), y=위도(lat) → Double 변환
                val markerList = places.mapNotNull {
                    val latDouble = it.y.toDoubleOrNull()
                    val lngDouble = it.x.toDoubleOrNull()
                    if (latDouble != null && lngDouble != null) {
                        latDouble to lngDouble
                    } else null
                }
                Log.d("NeighborhoodViewModel", "검색 키워드: $keyword, 결과 마커 수: ${markerList.size}")
                markerList.forEachIndexed { index, (markerLat, markerLng) ->
                    Log.d("NeighborhoodViewModel", "Marker #$index -> lat=$markerLat, lng=$markerLng")
                }

                _markers.value = markerList

            } catch (e: Exception) {
                Log.e("NeighborhoodViewModel", "검색 중 오류 발생", e)
            }
        }
    }
}
