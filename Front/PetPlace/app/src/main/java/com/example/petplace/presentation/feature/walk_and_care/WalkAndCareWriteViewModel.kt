package com.example.petplace.presentation.feature.walk_and_care

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.cares.CareCategory
import com.example.petplace.data.model.cares.CareCreateRequest
import com.example.petplace.data.repository.CaresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WalkAndCareWriteViewModel @Inject constructor(
    private val caresRepository: CaresRepository
) : ViewModel() {

    // UI 표시용 라벨(탭 순서)
    val categories = listOf("산책구인", "산책의뢰", "돌봄구인", "돌봄의뢰")

    // 입력 상태
    private val _pickedCat   = MutableStateFlow(categories.first())
    private val _title       = MutableStateFlow("")
    private val _details     = MutableStateFlow("")
    private val _date        = MutableStateFlow<LocalDate?>(null)
    private val _startTime   = MutableStateFlow<LocalTime?>(null)
    private val _endTime     = MutableStateFlow<LocalTime?>(null)
    private val _imageUris   = MutableStateFlow<List<Uri>>(emptyList())

    // 서버 필수값
    private val _petId       = MutableStateFlow<Long?>(null)
    private val _regionId    = MutableStateFlow<Long?>(null)

    // 공개 상태
    val pickedCat  : StateFlow<String>     = _pickedCat.asStateFlow()
    val title      : StateFlow<String>     = _title.asStateFlow()
    val details    : StateFlow<String>     = _details.asStateFlow()
    val date       : StateFlow<LocalDate?> = _date.asStateFlow()
    val startTime  : StateFlow<LocalTime?> = _startTime.asStateFlow()
    val endTime    : StateFlow<LocalTime?> = _endTime.asStateFlow()
    val imageUris  : StateFlow<List<Uri>>  = _imageUris.asStateFlow()
    val petId      : StateFlow<Long?>      = _petId.asStateFlow()
    val regionId   : StateFlow<Long?>      = _regionId.asStateFlow()

    // 버튼 활성화 조건
    @RequiresApi(Build.VERSION_CODES.O)
    val isValid: StateFlow<Boolean> =
        combine(_title, _details, _date, _startTime, _endTime, _petId, _regionId) { arr: Array<Any?> ->
            val t   = arr[0] as String
            val d   = arr[1] as String
            val dt  = arr[2] as LocalDate?
            val st  = arr[3] as LocalTime?
            val et  = arr[4] as LocalTime?
            val pid = arr[5] as Long?
            val rid = arr[6] as Long?

            t.isNotBlank() && d.isNotBlank() &&
                    dt != null && st != null && et != null &&
                    pid != null && rid != null
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // 로딩/이벤트
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _event = Channel<String>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // setters
    fun selectCategory(cat: String) { _pickedCat.value = cat }
    fun updateTitle(v: String)      { _title.value = v }
    fun updateDetails(v: String)    { _details.value = v }
    fun setDate(v: LocalDate)       { _date.value = v }
    fun setStartTime(v: LocalTime)  { _startTime.value = v }
    fun setEndTime(v: LocalTime)    { _endTime.value = v }
    fun setPetId(id: Long)          { _petId.value = id }
    fun setRegionId(id: Long)       { _regionId.value = id }

    fun addImages(uris: List<Uri>) {
        _imageUris.value = (_imageUris.value + uris).distinct().take(5)
    }
    fun removeImage(uri: Uri) { _imageUris.value = _imageUris.value - uri }
    fun clearImages()         { _imageUris.value = emptyList() }

    // 화면 표시용 포맷
    @RequiresApi(Build.VERSION_CODES.O)
    fun dateText(): String =
        _date.value?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) ?: "날짜를 선택해주세요"

    @RequiresApi(Build.VERSION_CODES.O)
    fun startText(): String =
        _startTime.value?.format(DateTimeFormatter.ofPattern("a HH:mm")) ?: "시작 시간"

    @RequiresApi(Build.VERSION_CODES.O)
    fun endText(): String =
        _endTime.value?.format(DateTimeFormatter.ofPattern("a HH:mm")) ?: "종료 시간"

    // 등록
    @RequiresApi(Build.VERSION_CODES.O)
    fun submit(
        onSuccess: (Long?) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val dt  = _date.value
        val st  = _startTime.value
        val et  = _endTime.value
        val pid = _petId.value
        val rid = _regionId.value

        if (dt == null || st == null || et == null || pid == null || rid == null) {
            val msg = "필수값이 비어있습니다. (날짜/시간/반려동물/지역)"
            onError(msg)
            viewModelScope.launch { _event.send(msg) }
            return
        }

        val req = CareCreateRequest(
            title = _title.value.trim(),
            content = _details.value.trim(),
            petId = pid,
            regionId = rid,
            category = mapLabelToEnum(_pickedCat.value),        // ✅ enum 그대로 전달
            startDate = dt.format(DateTimeFormatter.ISO_DATE),  // yyyy-MM-dd
            endDate   = dt.format(DateTimeFormatter.ISO_DATE),
            startTime = st.format(DateTimeFormatter.ofPattern("HH:mm")),
            endTime   = et.format(DateTimeFormatter.ofPattern("HH:mm")),
            // NOTE: 서버가 "이미지 URL"을 기대한다면, 업로드 후 받은 URL을 넣어야 함
            imageUrls = _imageUris.value.map { it.toString() }
        )

        viewModelScope.launch {
            _isSubmitting.value = true
            caresRepository.create(req)
                .onSuccess { resp ->
                    _isSubmitting.value = false
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        val ok = body?.success == true
                        val id = body?.data as? Long
                        if (ok) {
                            onSuccess(id)
                            _event.send("등록 완료")
                            clearForm()
                        } else {
                            val msg = body?.message ?: "등록 실패(서버 메시지 없음)"
                            onError(msg); _event.send(msg)
                        }
                    } else {
                        val msg = "HTTP ${resp.code()}: ${resp.errorBody()?.string()}"
                        onError(msg); _event.send(msg)
                    }
                }
                .onFailure { e ->
                    _isSubmitting.value = false
                    val msg = e.message ?: "네트워크 오류"
                    onError(msg); _event.send(msg)
                }
        }
    }

    // 라벨 -> enum (서버 스펙 고정)
    private fun mapLabelToEnum(label: String): CareCategory = when (label) {
        "산책구인" -> CareCategory.WALK_WANT   // @SerializedName("WALK_WANT")
        "산책의뢰" -> CareCategory.WALK_OFFER  // @SerializedName("WALK_REQ")
        "돌봄구인" -> CareCategory.CARE_WANT   // @SerializedName("CARE_WANT")
        "돌봄의뢰" -> CareCategory.CARE_REQ    // @SerializedName("CARE_REQ")
        else       -> CareCategory.WALK_WANT
    }

    // 필요하면 enum -> 라벨도 사용
    @Suppress("unused")
    private fun mapEnumToLabel(category: CareCategory): String = when (category) {
        CareCategory.WALK_WANT  -> "산책구인"
        CareCategory.WALK_OFFER -> "산책의뢰"
        CareCategory.CARE_WANT  -> "돌봄구인"
        CareCategory.CARE_REQ   -> "돌봄의뢰"
    }

    private fun clearForm() {
        _pickedCat.value = categories.first()
        _title.value = ""
        _details.value = ""
        _date.value = null
        _startTime.value = null
        _endTime.value = null
        _imageUris.value = emptyList()
        // pet/region은 유지할지 초기화할지 정책에 맞게 결정
    }
}
