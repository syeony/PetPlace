package com.example.petplace.presentation.feature.hotel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.video.AudioSpec.ChannelCount
import androidx.lifecycle.ViewModel
import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.data.model.hotel.HotelDetail
import com.example.petplace.data.model.hotel.HotelSearchResponse
import com.example.petplace.data.remote.HotelApiService
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.repository.HotelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.hotel.HotelReservationRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
data class HotelReservationState(
    val selectedAnimal: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val selectedHotelId: Int? = null,
    val animalCount: Int? = 1
)


@HiltViewModel
class HotelSharedViewModel @Inject constructor(
    private val hotelApi : HotelApiService
) : ViewModel() {

    // 상태를 StateFlow로 관리
    private val _reservationState = MutableStateFlow(HotelReservationState())
    val reservationState: StateFlow<HotelReservationState> = _reservationState
    val current = _reservationState.value.animalCount


    private val _hotelList = MutableStateFlow<List<HotelDetail>>(emptyList())
    val hotelList: StateFlow<List<HotelDetail>> = _hotelList

    private val _hotelDetail = MutableStateFlow<HotelDetail?>(null)
    val hotelDetail: StateFlow<HotelDetail?> = _hotelDetail

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    data class UiState(
        val loading: Boolean = false,
        val isAvailable: Boolean? = null,
        val createdReservationId: String? = null,
        val confirmed: Boolean = false
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event = Channel<String>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun selectAnimal(animal: String) {
        _reservationState.value = _reservationState.value.copy(selectedAnimal = animal)
    }
    init {
        android.util.Log.d("HotelVM", "HotelSharedViewModel 생성됨")
        android.util.Log.d("HotelVM", "초기 상태: ${_reservationState.value}")
    }
    fun increaseAnimalCount() {
        val current = _reservationState.value.animalCount ?: 1
        _reservationState.value = _reservationState.value.copy(
            animalCount = current + 1
        )
    }

    fun decreaseAnimalCount() {
        val current = _reservationState.value.animalCount ?: 1
        if (current > 0) { // 0 이하로 내려가지 않게
            _reservationState.value = _reservationState.value.copy(
                animalCount = current - 1
            )
        }
    }

    fun selectDate(checkIn: String, checkOut: String) {
        _reservationState.value = _reservationState.value.copy(
            checkInDate = checkIn,
            checkOutDate = checkOut
        )
    }

    fun selectHotel(hotelId: Int) {
        _reservationState.value = _reservationState.value.copy(selectedHotelId = hotelId)
    }

    fun reset() {
        _reservationState.value = HotelReservationState()
    }

    suspend fun getHotelDetail() {
        try {
            val resp = hotelApi.getHotelDetail(reservationState.value.selectedHotelId!!)
            if (!resp.isSuccessful) {
                _error.value = "HTTP ${resp.code()}: ${resp.errorBody()?.string()}"
                return
            }
            val body = resp.body() ?: run { _error.value = "빈 응답"; return }
            if (!body.success) { _error.value = body.message; return }
            _hotelDetail.value = body.data
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    suspend fun getHotelList(address: String = "강남") {
        try {
            val resp = hotelApi.searchHotelWithAddress(address) // Response<ApiResponse<List<HotelSearchResponse>>>
            if (!resp.isSuccessful) {
                _error.value = "HTTP ${resp.code()}: ${resp.errorBody()?.string()}"
                return
            }
            val body = resp.body() ?: run {
                _error.value = "빈 응답"
                return
            }
            if (!body.success) {
                _error.value = body.message
                return
            }
            _hotelList.value = body.data
        } catch (e: Exception) {
            _error.value = e.message
        }
    }

    // 호출: 입력 없이 상태에서 꺼내서 요청 보냄
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkReservationAvailability() = launchCatching {
        val state = _reservationState.value
        val hotelId = state.selectedHotelId ?: run {
            _event.send("호텔이 선택되지 않았어요.")
            return@launchCatching
        }
        val checkIn = state.checkInDate
        val checkOut = state.checkOutDate
        if (checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) {
            _event.send("체크인/체크아웃을 선택해주세요.")
            return@launchCatching
        }

        // 체크아웃 포함(예: 18~21 → 18,19,20,21)
        val dates = buildStayDatesInclusive(checkIn, checkOut) // "yyyy-MM-dd" 가정
        if (dates.isEmpty()) {
            _event.send("날짜 범위가 올바르지 않습니다.")
            return@launchCatching
        }

        val req = CheckReservationAvailabilityRequest(
            hotelId = hotelId,
            selectedDates = dates
        )

        val res = hotelApi.checkReservationAvailability(req)
        if (res.isSuccessful) {
            val available = res.body()?.data ?: false
            _uiState.update { it.copy(isAvailable = available) }
            if (!available) _event.send("선택한 일정은 예약이 불가능합니다.")
        } else {
            _event.send("예약 가능 여부 확인 실패 (${res.code()})")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    // 체크인~체크아웃 '포함'해서 날짜 리스트 생성 ("yyyy-MM-dd")
    private fun buildStayDatesInclusive(
        checkIn: String,
        checkOut: String,
        pattern: String = "yyyy-MM-dd"
    ): List<String> {
        return try {
            val fmt = java.time.format.DateTimeFormatter.ofPattern(pattern)
            var d = java.time.LocalDate.parse(checkIn, fmt)
            val end = java.time.LocalDate.parse(checkOut, fmt)

            if (d.isAfter(end)) return emptyList()

            val out = mutableListOf<String>()
            while (!d.isAfter(end)) { // ← inclusive
                out += d.format(fmt)
                d = d.plusDays(1)
            }
            out
        } catch (_: Throwable) {
            emptyList()
        }
    }


    fun makeHotelReservation(req: HotelReservationRequest) = launchCatching {
        val res = hotelApi.makeHotelReservation(req)
        if (res.isSuccessful) {
            val reservationId = res.body()?.data
            if (reservationId != null) {
                _uiState.update { it.copy(createdReservationId = reservationId) }
            } else {
                _event.send(res.body()?.message ?: "예약 생성 응답이 비어있습니다.")
            }
        } else {
            _event.send("호텔 예약 생성 실패 (${res.code()})")
        }
    }

    fun confirmReservation(reservationId: Long) = launchCatching {
        val res = hotelApi.confirmReservation(reservationId)
        if (res.success == true) {
            _uiState.update { it.copy(confirmed = true) }
            _event.send("예약이 확정되었습니다.")
        } else {
            _event.send("예약 확정 실패 (${res.success})")
        }
    }

    fun checkThenReserve(
        checkReq: CheckReservationAvailabilityRequest,
        createReq: HotelReservationRequest
    ) = launchCatching {
        val chk = hotelApi.checkReservationAvailability(checkReq)
        if (!chk.isSuccessful || chk.body()?.data != true) {
            _event.send("해당 일정은 예약할 수 없습니다.")
            _uiState.update { it.copy(isAvailable = false) }
            return@launchCatching
        }
        _uiState.update { it.copy(isAvailable = true) }

        val crt = hotelApi.makeHotelReservation(createReq)
        if (crt.isSuccessful) {
            val reservationId = crt.body()?.data
            if (reservationId != null) {
                _uiState.update { it.copy(createdReservationId = reservationId) }
            } else {
                _event.send(crt.body()?.message ?: "예약 생성 응답이 비어있습니다.")
            }
        } else {
            _event.send("호텔 예약 생성 실패 (${crt.code()})")
        }
    }

    private fun launchCatching(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            _uiState.update { it.copy(loading = true) }
            block()
        } catch (t: Throwable) {
            _event.send(t.message ?: "알 수 없는 오류")
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
    }
}




