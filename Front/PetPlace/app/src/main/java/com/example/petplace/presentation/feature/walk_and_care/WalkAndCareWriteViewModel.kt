// presentation/feature/walk_and_care/WalkAndCareWriteViewModel.kt
package com.example.petplace.presentation.feature.walk_and_care

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.cares.CareCategory
import com.example.petplace.data.model.cares.CareCreateRequest
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.remote.UserApiService
import com.example.petplace.data.repository.CaresRepository
import com.example.petplace.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WalkAndCareWriteViewModel @Inject constructor(
    private val caresRepository: CaresRepository,
    private val imageRepository: ImageRepository,
    private val userApi: UserApiService, // ✅ Repo 대신 ApiService 직접 주입
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun updateRegionByLocation(context: Context) {
        viewModelScope.launch {
            val fine = android.Manifest.permission.ACCESS_FINE_LOCATION
            val coarse = android.Manifest.permission.ACCESS_COARSE_LOCATION
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, fine) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, coarse) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                val loc = LocationProvider.getCurrentLocation(context)
                if (loc != null) {
                    Log.d(TAG, "GPS lat=${loc.latitude}, lon=${loc.longitude}")
                    try {
                        val res = userApi.authenticateDong(loc.latitude, loc.longitude)
                        if (res.success && res.data != null) {
                            _regionId.value = res.data.regionId
                            Log.d(TAG, "regionId 업데이트 성공: ${res.data.regionId}, name=${res.data.regionName}")
                        } else {
                            Log.e(TAG, "동네 인증 실패: ${res.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "동네 인증 API 오류", e)
                    }
                } else {
                    Log.e(TAG, "현재 위치를 가져오지 못했습니다 (null)")
                }
            }
        }
    }

    val app = context as PetPlaceApp
    val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")

    private val TAG = "WalkAndCareWrite"

    // UI 표시용 라벨(탭 순서)
    val categories = listOf("산책구인", "산책의뢰", "돌봄구인", "돌봄의뢰")

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

    // ---------- 입력 상태 ----------
    private val _pickedCat    = MutableStateFlow(categories.first())
    private val _title        = MutableStateFlow("")
    private val _details      = MutableStateFlow("")
    // 산책 전용
    private val _startTime    = MutableStateFlow<LocalTime?>(null)
    private val _endTime      = MutableStateFlow<LocalTime?>(null)
    // 돌봄 전용
    private val _startDate    = MutableStateFlow<LocalDate?>(null)
    private val _endDate      = MutableStateFlow<LocalDate?>(null)

    // 선택한 로컬 사진 URI들
    private val _imageUris    = MutableStateFlow<List<Uri>>(emptyList())

    // 서버 필수값
//    private val _petId        = MutableStateFlow<Long?>(null)
    private val _regionId     = MutableStateFlow<Long?>(null)

    init {
        // ✅ 로그인 유저의 regionId를 초기값으로 세팅
        _regionId.value = user.regionId
    }

    // ---------- 공개 State ----------
    val pickedCat : StateFlow<String>      = _pickedCat.asStateFlow()
    val title     : StateFlow<String>      = _title.asStateFlow()
    val details   : StateFlow<String>      = _details.asStateFlow()
    val startTime : StateFlow<LocalTime?>  = _startTime.asStateFlow()
    val endTime   : StateFlow<LocalTime?>  = _endTime.asStateFlow()
    val startDate : StateFlow<LocalDate?>  = _startDate.asStateFlow()
    val endDate   : StateFlow<LocalDate?>  = _endDate.asStateFlow()
    val imageUris : StateFlow<List<Uri>>   = _imageUris.asStateFlow()
//    val petId     : StateFlow<Long?>       = _petId.asStateFlow()
    val regionId  : StateFlow<Long?>       = _regionId.asStateFlow()

    // ---------- 모드 파생 ----------
    private val mode: StateFlow<Mode> = pickedCat
        .map { label ->
            when (label) {
                "산책구인", "산책의뢰" -> Mode.WALK
                "돌봄구인", "돌봄의뢰" -> Mode.CARE
                else -> Mode.NONE
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Mode.WALK)

    @RequiresApi(Build.VERSION_CODES.O)
    val isValid: StateFlow<Boolean> =
        combine(
            title, details, mode, startTime, endTime, startDate, endDate, petId, regionId
        ) { values: Array<Any?> ->
            val t   = values[0] as String
            val d   = values[1] as String
            val m   = values[2] as Mode
            val st  = values[3] as LocalTime?
            val et  = values[4] as LocalTime?
            val sd  = values[5] as LocalDate?
            val ed  = values[6] as LocalDate?
            val pid = values[7] as Long?
            val rid = values[8] as Long?

            val baseOk = t.isNotBlank() && d.isNotBlank() && pid != null && rid != null
            if (!baseOk) {
                false
            } else {
                when (m) {
                    Mode.WALK -> st != null && et != null && !et.isBefore(st)
                    Mode.CARE -> sd != null && ed != null && !ed.isBefore(sd)
                    Mode.NONE -> false
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ---------- 로딩/이벤트 ----------
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _event = Channel<String>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // ---------- setters ----------
    fun selectCategory(cat: String) { _pickedCat.value = cat }

    fun updateTitle(v: String)   { _title.value = v }
    fun updateDetails(v: String) { _details.value = v }

    fun setStartTime(v: LocalTime?) { _startTime.value = v }
    fun setEndTime(v: LocalTime?)   { _endTime.value = v }

    fun setStartDate(v: LocalDate?) { _startDate.value = v }
    fun setEndDate(v: LocalDate?)   { _endDate.value = v }

//    fun setPetId(id: Long)     { _petId.value = id }
    fun setRegionId(id: Long)  { _regionId.value = id }

    fun addImages(uris: List<Uri>) {
        val merged = (_imageUris.value + uris).distinct().take(5)
        _imageUris.value = merged
        Log.d(TAG, "이미지 추가. 총 ${merged.size}개")
        merged.forEachIndexed { i, u -> Log.d(TAG, "  [URI $i] $u") }
    }
    fun removeImage(uri: Uri) {
        _imageUris.value = _imageUris.value - uri
        Log.d(TAG, "이미지 제거: $uri, 남은 개수=${_imageUris.value.size}")
    }
    fun clearImages() {
        _imageUris.value = emptyList()
        Log.d(TAG, "이미지 전부 초기화")
    }

    // ---------- 화면 표시용 포맷 ----------
    @RequiresApi(Build.VERSION_CODES.O)
    fun startTimeText(): String =
        _startTime.value?.format(DateTimeFormatter.ofPattern("a HH:mm")) ?: "시작 시간"

    @RequiresApi(Build.VERSION_CODES.O)
    fun endTimeText(): String =
        _endTime.value?.format(DateTimeFormatter.ofPattern("a HH:mm")) ?: "종료 시간"

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateRangeText(): String {
        val s = _startDate.value?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        val e = _endDate.value?.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        return if (s == null && e == null) "기간을 선택해주세요"
        else listOfNotNull(s, e).joinToString(" ~ ")
    }

    // ---------- 등록 ----------
    @RequiresApi(Build.VERSION_CODES.O)
    fun submit(
        onSuccess: (Long?) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_isSubmitting.value) return // ✅ 중복 제출 방지

        val currentMode = mode.value
        val pid = _petId.value
        val rid = _regionId.value ?: user.regionId
        val t = _title.value.trim()
        val c = _details.value.trim()

        if (t.isBlank() || c.isBlank() || pid == null || rid == null) {
            val msg = "필수값이 비어있습니다. (제목/내용/반려동물/지역)"
            onError(msg)
            viewModelScope.launch { _event.send(msg) }
            return
        }

        val dateFmt = DateTimeFormatter.ISO_DATE
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                // 1) 이미지 업로드 (로그 상세)
                val localUris = _imageUris.value
                Log.d(TAG, "업로드 시작: 로컬 URI ${localUris.size}개")
                localUris.forEachIndexed { i, u -> Log.d(TAG, "  [업로드 대상 $i] $u") }

                // 1) 이미지 업로드
                val uploadedUrls =
                    if (localUris.isNotEmpty())
                        imageRepository.uploadImages(localUris)
                    else emptyList()

                // 2) ImageInfo 리스트로 변환
                val imageInfos = uploadedUrls.mapIndexed { index, url ->
                    MyPageInfoResponse.ImageInfo(
                        id = null,   // 새 업로드는 id 없음
                        src = url,   // 업로드된 서버 URL
                        sort = index // 업로드 순서
                    )
                }


                Log.d(TAG, "업로드 완료: 서버 URL ${uploadedUrls.size}개 (distinct=${uploadedUrls.toSet().size})")
                uploadedUrls.forEachIndexed { i, u -> Log.d(TAG, "  [서버 URL $i] $u") }

                // 업로드 요청했는데 URL이 0개면 실패로 간주
                if (localUris.isNotEmpty() && uploadedUrls.isEmpty()) {
                    throw IllegalStateException("이미지 업로드에 실패했습니다.")
                }

                // 개수/중복 경고(프로필 이미지가 반복 들어가는 케이스 조기 포착)
                if (uploadedUrls.isNotEmpty()) {
                    if (uploadedUrls.size != localUris.size) {
                        Log.w(TAG, "경고: 업로드 결과 개수가 입력 개수와 다름 (input=${localUris.size}, result=${uploadedUrls.size})")
                    }
                    val dupCount = uploadedUrls.size - uploadedUrls.toSet().size
                    if (dupCount > 0) {
                        Log.w(TAG, "경고: 업로드 URL에 중복 감지됨 (dup=$dupCount)")
                    }
                }

                // 2) 바디 생성
                val req: CareCreateRequest = when (currentMode) {
                    Mode.WALK -> {
                        val st = _startTime.value
                        val et = _endTime.value
                        if (st == null || et == null) {
                            val msg = "산책: 시작/종료 시간을 선택하세요."
                            onError(msg); _event.send(msg); _isSubmitting.value = false; return@launch
                        }
                        if (et.isBefore(st)) {
                            val msg = "산책: 종료 시간이 시작 시간보다 빠릅니다."
                            onError(msg); _event.send(msg); _isSubmitting.value = false; return@launch
                        }
                        CareCreateRequest(
                            title     = t,
                            content   = c,
                            petId     = pid,
                            regionId  = rid,
                            category  = mapLabelToEnum(_pickedCat.value),
                            startDate = LocalDate.now().format(dateFmt),
                            endDate   = null,
                            startTime = st.format(timeFmt),
                            endTime   = et.format(timeFmt),
                            images = imageInfos
                        ).also { Log.d(TAG, "요청 바디(산책): $it") }
                    }
                    Mode.CARE -> {
                        val sd = _startDate.value
                        val ed = _endDate.value
                        if (sd == null || ed == null) {
                            val msg = "돌봄: 시작/종료 날짜를 선택하세요."
                            onError(msg); _event.send(msg); _isSubmitting.value = false; return@launch
                        }
                        if (ed.isBefore(sd)) {
                            val msg = "돌봄: 종료 날짜가 시작 날짜보다 빠릅니다."
                            onError(msg); _event.send(msg); _isSubmitting.value = false; return@launch
                        }
                        CareCreateRequest(
                            title     = t,
                            content   = c,
                            petId     = pid,
                            regionId  = rid,
                            category  = mapLabelToEnum(_pickedCat.value),
                            startDate = sd.format(dateFmt),
                            endDate   = ed.format(dateFmt),
                            startTime = null,
                            endTime   = null,
                            images = imageInfos
                        ).also { Log.d(TAG, "요청 바디(돌봄): $it") }
                    }
                    else -> {
                        onError("카테고리를 선택해주세요.")
                        _isSubmitting.value = false
                        return@launch
                    }
                }

                // 3) 등록 호출
                caresRepository.create(req)
                    .onSuccess { resp ->
                        _isSubmitting.value = false
                        if (resp.isSuccessful) {
                            val body = resp.body()
                            val ok = body?.success == true
                            val id = body?.data as? Long
                            Log.d(TAG, "등록 응답: http=${resp.code()} success=$ok id=$id body=$body")
                            if (ok) {
                                onSuccess(id)
                                _event.send("등록 완료")
                                clearForm(keepPetAndRegion = true)
                            } else {
                                val msg = body?.message ?: "등록 실패(서버 메시지 없음)"
                                onError(msg); _event.send(msg)
                            }
                        } else {
                            val msg = "HTTP ${resp.code()}: ${resp.errorBody()?.string()}"
                            Log.e(TAG, "등록 실패: $msg")
                            onError(msg); _event.send(msg)
                        }
                    }
                    .onFailure { e ->
                        _isSubmitting.value = false
                        val msg = e.message ?: "네트워크 오류"
                        Log.e(TAG, "등록 실패(onFailure): $msg", e)
                        onError(msg); _event.send(msg)
                    }

            } catch (e: Exception) {
                _isSubmitting.value = false
                val msg = e.message ?: "업로드 실패"
                Log.e(TAG, "submit 예외: $msg", e)
                onError(msg); _event.send(msg)
            }
        }
    }

    // ---------- 매핑 ----------
    private fun mapLabelToEnum(label: String): CareCategory = when (label) {
        "산책구인" -> CareCategory.WALK_WANT
        "산책의뢰" -> CareCategory.WALK_OFFER
        "돌봄구인" -> CareCategory.CARE_WANT
        "돌봄의뢰" -> CareCategory.CARE_REQ
        else       -> CareCategory.WALK_WANT
    }

    // ---------- 유틸 ----------
    private fun clearForm(keepPetAndRegion: Boolean) {
        _pickedCat.value = categories.first()
        _title.value = ""
        _details.value = ""
        _startTime.value = null
        _endTime.value = null
        _startDate.value = null
        _endDate.value = null
        _imageUris.value = emptyList()
        if (!keepPetAndRegion) {
            _petId.value = null
            _regionId.value = null
        }
    }

    private enum class Mode { WALK, CARE, NONE }
}
