package com.example.petplace.presentation.feature.missing_list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.PetPlaceApp
import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.model.missing_list.MissingReportDto
import com.example.petplace.data.remote.ChatApiService
import com.example.petplace.data.repository.MissingSightingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import javax.inject.Inject

data class MissingListUiState(
    val isLoading: Boolean = false,
    val items: List<MissingReportUi> = emptyList(),
    val error: String? = null,
    val page: Int = 0,
    val size: Int = 20,
    val hasMore: Boolean = false,
    val createdChatRoomId: Long? = null,
    val isChatRoomCreating: Boolean = false
)

data class MissingReportUi(
    val id: Long,
    val userId: Long,
    val reporterName: String,
    val reporterAvatarUrl: String?,
    val content: String,
    val photoUrl: String?,
    val seenAt: String,
    val location: String
)

@HiltViewModel
class MissingListViewModel @Inject constructor(
    private val repo: MissingSightingRepository,
    private val chatApiService: ChatApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 앱/유저 정보
    private val app = context as PetPlaceApp
    private val user = app.getUserInfo() ?: throw IllegalStateException("로그인 필요")
    private val regionId: Long = user.regionId

    // 상태
    private val _ui = MutableStateFlow(MissingListUiState())
    val ui: StateFlow<MissingListUiState> = _ui

    init {
        load(page = 0, size = 20, sort = "createdAt,desc")
    }

    fun load(page: Int = 0, size: Int = 20, sort: String = "createdAt,desc") {
        if (_ui.value.isLoading) return
        _ui.value = _ui.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            runCatching { repo.fetchMissingReports(regionId, page, size, sort) }
                .onSuccess { res ->
                    val body = res.data
                    if (res.success && body != null) {
                        val mapped = body.content.map { it.toUiInline() }
                        _ui.value = _ui.value.copy(
                            isLoading = false,
                            items = if (page == 0) mapped else _ui.value.items + mapped,
                            page = page,
                            size = size,
                            hasMore = !body.last,
                            error = null
                        )
                    } else {
                        _ui.value = _ui.value.copy(
                            isLoading = false,
                            error = (res.message ?: "").ifBlank { "목록을 불러오지 못했습니다." }
                        )
                    }
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(
                        isLoading = false,
                        error = e.message ?: "네트워크 오류가 발생했습니다."
                    )
                }
        }
    }

    fun loadNext(sort: String = "createdAt,desc") {
        val s = _ui.value
        if (!s.hasMore || s.isLoading) return
        load(page = s.page + 1, size = s.size, sort = sort)
    }

    // ========= 헬퍼들 (ViewModel 내부) =========

    /** DTO -> UI 변환 (널/상대경로/날짜 대응) */
    private fun MissingReportDto.toUiInline(): MissingReportUi {
        // 대표 이미지
        val firstImage = images.firstOrNull()?.src
        val rawPhoto: String? = when {
            !firstImage.isNullOrBlank() -> firstImage
            !petImg.isNullOrBlank()     -> petImg
            else                        -> null
        }
        val photo = rawPhoto?.let { normalizeUrl(it) }

        // 아바타 이미지 (null/blank 안전)
        val avatar = userImg?.takeIf { it.isNotBlank() }?.let { normalizeUrl(it) }

        return MissingReportUi(
            id = id,
            userId = userId,
            reporterName = userNickname,
            reporterAvatarUrl = avatar,
            content = content,
            photoUrl = photo,
            seenAt = formatToKSTFull(missingAt),
            location = address.ifBlank { regionName }
        )
    }

    /** 서버가 /images/... 로 주면 BASE_URL 붙여 절대경로로 */
    private fun normalizeUrl(src: String): String {
        if (src.startsWith("http://") || src.startsWith("https://")) return src
        // 필요 시 BuildConfig.BASE_URL 사용 가능. 지금은 서버 주소 하드코딩 예시.
        val base = "http://i13d104.p.ssafy.io:8081"
        return base.trimEnd('/') + src // src가 /로 시작하므로 바로 붙임
    }

    // ====== 공통 설정 ======
    private val KST_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    private val TIME_ONLY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREA)
    private val DATE_TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a HH:mm", Locale.KOREA)

    // 유연한 입력 파서: "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]['Z']"
    private val FLEXIBLE_INPUT: DateTimeFormatter =
        DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart().appendPattern(".SSS").optionalEnd()
            .optionalStart().appendOffsetId().optionalEnd() // +09:00 같은 오프셋
            .optionalStart().appendLiteral('Z').optionalEnd() // Z (UTC)
            .toFormatter(Locale.ROOT)

    // ====== 공통 변환 함수 (원하는 출력 포맷만 주입) ======
    private fun formatToKST(input: String, outFmt: DateTimeFormatter): String {
        return try {
            val parsed = FLEXIBLE_INPUT.parseBest(
                input,
                { OffsetDateTime.from(it) },
                { ZonedDateTime.from(it) },
                { LocalDateTime.from(it) }
            )
            val kstZdt: ZonedDateTime = when (parsed) {
                is OffsetDateTime -> parsed.atZoneSameInstant(KST_ZONE)
                is ZonedDateTime  -> parsed.withZoneSameInstant(KST_ZONE)
                is LocalDateTime  -> parsed.atOffset(ZoneOffset.UTC).atZoneSameInstant(KST_ZONE) // 오프셋 없으면 UTC로 간주
                else -> return input
            }
            kstZdt.format(outFmt)
        } catch (_: Exception) {
            input // 실패 시 원문
        }
    }

    // ====== 편의 래퍼 ======
    fun formatToHHmmKST(input: String): String = formatToKST(input, TIME_ONLY_FMT)
    fun formatToKSTFull(input: String): String = formatToKST(input, DATE_TIME_FMT)

    fun startChatWithUser(userId: Long) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isChatRoomCreating = true)

                val result = createChatRoom(userId)
                result.onSuccess { chatRoomResponse ->
                    _ui.value = _ui.value.copy(
                        createdChatRoomId = chatRoomResponse.chatRoomId,
                        isChatRoomCreating = false
                    )
                }.onFailure { exception ->
                    _ui.value = _ui.value.copy(
                        error = exception.message ?: "채팅방 생성에 실패했습니다.",
                        isChatRoomCreating = false
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    error = e.message ?: "채팅방 생성 중 오류가 발생했습니다.",
                    isChatRoomCreating = false
                )
            }
        }
    }

    private suspend fun createChatRoom(userId: Long): Result<ChatRoomResponse> {
        val myId = user.userId ?: 0
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApiService.createChatRoom(
                    CreateChatRoomRequest(userId1 = myId, userId2 = userId)
                )

                if (response.isSuccessful) {
                    val chatRoom = response.body()
                    if (chatRoom != null) {
                        Result.success(chatRoom)
                    } else {
                        Result.failure(Exception("채팅방 생성 응답이 null"))
                    }
                } else {
                    Result.failure(Exception("채팅방 생성 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun consumeCreatedChatRoomId() {
        _ui.value = _ui.value.copy(createdChatRoomId = null)
    }

}
