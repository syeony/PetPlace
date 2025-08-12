package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.missing_report.CreateSightingImageReq
import com.example.petplace.data.model.missing_report.CreateSightingReq
import com.example.petplace.data.model.missing_report.SightingRes
import com.example.petplace.data.repository.ImageRepository
import com.example.petplace.data.repository.MissingSightingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class ReportUiState(
    val description: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now(),
    val selectedAddress: String = "위치 정보를 가져오는 중...",
    val selectedLat: Double? = null,
    val selectedLng: Double? = null,
    val hasManualSelection: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val submitted: Boolean = false
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repo: MissingSightingRepository,
    private val imageRepo: ImageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    val app = context as PetPlaceApp
    val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    fun updateDescription(text: String) {
        _uiState.value = _uiState.value.copy(description = text)
    }

    fun setDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun setTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }

    fun setAutoAddress(address: String, lat: Double?, lng: Double?) {
        val cur = _uiState.value
        if (cur.hasManualSelection) return
        _uiState.value = cur.copy(selectedAddress = address, selectedLat = lat, selectedLng = lng)
    }

    fun setManualAddress(address: String, lat: Double?, lng: Double?) {
        _uiState.value = _uiState.value.copy(
            selectedAddress = address,
            selectedLat = lat,
            selectedLng = lng,
            hasManualSelection = true
        )
    }

    /** 목격 제보 등록 */
    fun submitSighting(
        imageUrls: List<String>,           // 이미 업로드된 이미지 URL들이라고 가정
        onSuccess: (SightingRes) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val s = _uiState.value

        val lat = s.selectedLat
        val lng = s.selectedLng
        if (lat == null || lng == null) {
            onFailure("좌표가 없습니다. 위치를 선택해주세요.")
            return
        }

        val address = s.selectedAddress.ifBlank { "주소 미확인" }

        // LocalDate + LocalTime → UTC ISO-8601(Z)
        val localDt: LocalDateTime = LocalDateTime.of(s.selectedDate, s.selectedTime)
        val instantUtc = localDt.atZone(ZoneId.systemDefault()).toInstant()
        val sightedAtIsoUtc = instantUtc.toString() // "2025-08-12T07:14:30.553Z" 형태

        val images = imageUrls.mapIndexed { idx, url ->
            CreateSightingImageReq(src = url, sort = idx + 1)
        }

        val req = CreateSightingReq(
            regionId = user.regionId,
            address = address,
            latitude = lat,
            longitude = lng,
            content = s.description,
            sightedAt = sightedAtIsoUtc,
            images = images
        )

        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null)
            repo.createSighting(req)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(loading = false, submitted = true)
                    onSuccess(it)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(loading = false, error = e.message)
                    onFailure(e.message ?: "등록에 실패했습니다.")
                }
        }
    }

    /** (추가) 갤러리 Uri들 -> 업로드 API로 URL 변환 -> 제보 등록 */
    fun submitSightingFromUris(
        imageUris: List<Uri>,
        onSuccess: (SightingRes) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null)
            // 1) 업로드
            val urlResult = runCatching { imageRepo.uploadImages(imageUris) }
            val urls = urlResult.getOrElse { e ->
                _uiState.value = s.copy(loading = false, error = e.message)
                onFailure(e.message ?: "이미지 업로드에 실패했습니다.")
                return@launch
            }
            // 2) Sightings 등록
            submitSighting(
                imageUrls = urls,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }
}
