// RegisterViewModel.kt
package com.example.petplace.presentation.feature.missing_register

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.missing_register.CreateRegisterImageReq
import com.example.petplace.data.model.missing_register.CreateRegisterReq
import com.example.petplace.data.repository.ImageRepository
import com.example.petplace.data.repository.MissingSightingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val imageRepo: ImageRepository,
    private val missingRepo: MissingSightingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val app = context as PetPlaceApp
    val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    // ── 선택된 펫(필수) ──
    private val _petId = MutableStateFlow<Long?>(null)
    val petId = _petId.asStateFlow()
    fun setPetId(id: Long) { _petId.value = id }

    // ── 선택된 펫 상태(이미 표시용으로 쓰던 부분 유지) ──
    private val _petName     = MutableStateFlow<String?>(null); val petName = _petName.asStateFlow()
    private val _petBreed    = MutableStateFlow<String?>(null); val petBreed = _petBreed.asStateFlow()
    private val _petSex      = MutableStateFlow<String?>(null); val petSex = _petSex.asStateFlow()
    private val _petBirthday = MutableStateFlow<String?>(null); val petBirthday = _petBirthday.asStateFlow()
    private val _petImgSrc   = MutableStateFlow<String?>(null); val petImgSrc = _petImgSrc.asStateFlow()
    fun setSelectedPet(name:String?, breed:String?, sex:String?, birthday:String?, imgSrc:String?, id:Long?=null) {
        _petName.value=name; _petBreed.value=breed; _petSex.value=sex
        _petBirthday.value=birthday; _petImgSrc.value=imgSrc
        id?.let { _petId.value = it }
    }

    // ── 위치 ──
    private val _lat = MutableStateFlow<Double?>(null); val lat = _lat.asStateFlow()
    private val _lng = MutableStateFlow<Double?>(null); val lng = _lng.asStateFlow()
    fun setLatLng(lat: Double, lng: Double) { _lat.value = lat; _lng.value = lng }

    // 서버에서 regionId를 요구. 없으면 0으로.
    private val _regionId = MutableStateFlow<Long>(0L)
    val regionId = _regionId.asStateFlow()
    fun setRegionId(id: Long) { _regionId.value = id }

    // ── 폼 데이터 ──
    private val _detail = MutableStateFlow(""); val detail = _detail.asStateFlow()
    fun setDetail(text: String) { _detail.value = text }

    private val _imageList = MutableStateFlow<List<Uri>>(emptyList()); val imageList = _imageList.asStateFlow()
    fun addImages(uris: List<Uri>) { _imageList.value = (_imageList.value + uris).distinct().take(5) }
    fun clearImages() { _imageList.value = emptyList() }

    var date = MutableStateFlow("yyyy년 MM월 dd일")
        private set
    var time = MutableStateFlow("오전 12:00")
        private set
    var place = MutableStateFlow("주소 선택")
        private set

    fun setDate(str: String)  { date.value  = str }
    fun setTime(str: String)  { time.value  = str }
    fun setPlace(str: String) { place.value = str }

    // ── UI 상태 ──
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()
    private val _submitError = MutableStateFlow<String?>(null)
    // ✅ 감지된 이미지 상태
    private val _detectionImageUri = MutableStateFlow<Uri?>(null)
    val detectionImageUri: StateFlow<Uri?> = _detectionImageUri.asStateFlow()

    // ✅ 감지 완료 시(외부에서 uri 넘겨줌)
    fun onDetectionDone(uri: Uri?) { _detectionImageUri.value = uri }

    // ✅ 감지 카드 닫기 버튼에서 호출
    fun clearDetection() { _detectionImageUri.value = null }
    // 진입 시 현재 시간 기본값 세팅
    fun setNowIfEmpty(now: ZonedDateTime = ZonedDateTime.now()) {
        if (date.value == "yyyy년 MM월 dd일") {
            date.value = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        }
        if (time.value == "오전 12:00" || time.value.isBlank()) {
            time.value = now.format(DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN))
        }
    }

    init {
        // 앱/화면 최초 진입 시 한 번, 현재 날짜/시간으로 기본값 세팅
        setNowIfEmpty()
    }

    // 화면의 한글 포맷(date/time) → 서버가 요구하는 ISO_INSTANT(Z)로 변환
    private fun buildMissingAtIso(
        dateKo: String,
        timeKo: String,
        zone: ZoneId = ZoneId.systemDefault()
    ): String {
        val d = LocalDate.parse(dateKo, DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        val t = LocalTime.parse(timeKo, DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN))
        val zdt = ZonedDateTime.of(d, t, zone)
        return DateTimeFormatter.ISO_INSTANT.format(zdt.toInstant()) // "2025-08-13T04:20:17Z"
    }

    // 제출
    fun submitRegister(
        onSuccess: (Long) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                _submitError.value = null

                val pid = petId.value ?: throw IllegalStateException("반려동물을 선택해주세요.")
                val la = lat.value ?: throw IllegalStateException("위치 권한/현재 위치가 필요합니다.")
                val ln = lng.value ?: throw IllegalStateException("위치 권한/현재 위치가 필요합니다.")
                val addr = place.value.ifBlank { throw IllegalStateException("주소를 선택/확인해주세요.") }

                // 1) 이미지 업로드
                val urls = imageRepo.uploadImages(imageList.value)

                // 2) 요청 바디 구성
                val req = CreateRegisterReq(
                    petId = pid,
                    regionId = user.regionId,        // 필요 시 setRegionId로 주입
                    address = addr,
                    latitude = la,
                    longitude = ln,
                    content = detail.value,
                    missingAt = buildMissingAtIso(date.value, time.value),
                    images = urls.mapIndexed { idx, url ->
                        CreateRegisterImageReq(src = url, sort = idx) // 0-based 정렬
                    }
                )

                // 3) API 호출
                val res = missingRepo.createRegister(req).getOrThrow()
                onSuccess(res.id)
            } catch (e: Exception) {
                _submitError.value = e.message ?: "등록 중 오류가 발생했습니다."
                onFailure(_submitError.value!!)
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
