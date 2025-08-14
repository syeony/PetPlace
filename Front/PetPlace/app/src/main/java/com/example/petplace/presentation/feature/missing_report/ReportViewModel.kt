package com.example.petplace.presentation.feature.missing_report

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt

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
    val submitted: Boolean = false,

    // YOLO 상태
    val detectionChecked: Boolean = false,
    val detectionMessage: String = ""
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repo: MissingSightingRepository,
    private val imageRepo: ImageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    private val app = context as PetPlaceApp
    private val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    private val detector by lazy { Yolo11nTFLite(context) }

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

    /** 갤러리 Uri들을 순차 감지 → 첫 성공시 로그로 바운딩박스+conf 전부 출력, 전부 실패면 메시지 표시 */
    fun analyzeImagesForPet(imageUris: List<Uri>) {
        viewModelScope.launch {
            Log.d("YOLO11N", "analyzeImagesForPet(): start, count=${imageUris.size}")

            if (imageUris.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    detectionChecked = true,
                    detectionMessage = "이미지가 없습니다."
                )
                Log.d("YOLO11N", "analyzeImagesForPet(): end (no images)")
                return@launch
            }

            _uiState.value = _uiState.value.copy(detectionChecked = false, detectionMessage = "분석 중...")

            var detectedMsg = "강아지/고양이 없음"
            var success = false
            val tAll0 = SystemClock.elapsedRealtime()

            for ((idx, uri) in imageUris.withIndex()) {
                Log.d("YOLO11N", "image[$idx]: $uri")

                val tDec0 = SystemClock.elapsedRealtime()
                val bmp = withContext(Dispatchers.IO) { detector.decodeBitmapFromUri(uri) }
                val tDec = SystemClock.elapsedRealtime() - tDec0

                if (bmp == null) {
                    Log.w("YOLO11N", "image[$idx]: decode FAILED (t=${tDec}ms)")
                    continue
                } else {
                    Log.d("YOLO11N", "image[$idx]: decode OK ${bmp.width}x${bmp.height} (t=${tDec}ms)")
                }

                val tInf0 = SystemClock.elapsedRealtime()
                val dets = withContext(Dispatchers.Default) { detector.detect(bmp) }
                val tInf = SystemClock.elapsedRealtime() - tInf0
                Log.d("YOLO11N", "image[$idx]: inference done, detCount=${dets.size} (t=${tInf}ms)")

                if (dets.isNotEmpty()) {
                    success = true
                    Log.d("YOLO11N", "===== DETECTED on image[$idx] =====")
                    dets.forEachIndexed { i, d ->
                        val xmin = d.left.coerceIn(0f, (bmp.width - 1).toFloat()).roundToInt()
                        val ymin = d.top.coerceIn(0f, (bmp.height - 1).toFloat()).roundToInt()
                        val xmax = d.right.coerceIn(0f, (bmp.width - 1).toFloat()).roundToInt()
                        val ymax = d.bottom.coerceIn(0f, (bmp.height - 1).toFloat()).roundToInt()
                        val wFace = d.score.toDouble() // confidence
                        Log.d(
                            "YOLO11N",
                            "[$i] label=${d.label}, wFace=${"%.3f".format(wFace)}, box=($xmin,$ymin,$xmax,$ymax)"
                        )
                    }
                    detectedMsg = "감지됨: " + dets.joinToString { "${it.label} ${"%.2f".format(it.score)}" } + " (idx=$idx)"
                    break
                } else {
                    Log.d("YOLO11N", "image[$idx]: NO DETECTION")
                }
            }

            val tAll = SystemClock.elapsedRealtime() - tAll0
            Log.d("YOLO11N", "analyzeImagesForPet(): end, total=${tAll}ms, success=$success, message=$detectedMsg")

            _uiState.value = _uiState.value.copy(
                detectionChecked = true,
                detectionMessage = detectedMsg
            )
        }
    }

    /** 제보 등록 (이미 업로드된 URL 사용) */
    fun submitSighting(
        imageUrls: List<String>,
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

        val localDt = LocalDateTime.of(s.selectedDate, s.selectedTime)
        val instantUtc = localDt.atZone(ZoneId.systemDefault()).toInstant()
        val sightedAtIsoUtc = instantUtc.toString()

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

    /** Uri들을 서버에 업로드 → URL 리스트 획득 → 제보 등록 */
    fun submitSightingFromUris(
        imageUris: List<Uri>,
        onSuccess: (SightingRes) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.value = s.copy(loading = true, error = null)
            val urls = runCatching { imageRepo.uploadImages(imageUris) }.getOrElse { e ->
                _uiState.value = s.copy(loading = false, error = e.message)
                onFailure(e.message ?: "이미지 업로드에 실패했습니다.")
                return@launch
            }
            submitSighting(
                imageUrls = urls,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }
}
