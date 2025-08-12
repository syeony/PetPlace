package com.example.petplace.presentation.feature.hotel

import android.os.Build
import android.util.Log
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
import com.example.petplace.data.model.payment.PreparePaymentRequest
import com.example.petplace.data.model.payment.PreparePaymentResponse
import com.example.petplace.data.model.payment.VerifyPaymentRequest
import com.example.petplace.data.remote.PaymentsApiService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HotelReservationState(
    val selectedAnimal: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val selectedHotelId: Int? = null,
    val animalCount: Int? = 1,
    val selectedPetId: Int = 0
)


@HiltViewModel
class HotelSharedViewModel @Inject constructor(
    private val hotelApi : HotelApiService,
    private val paymentsApi : PaymentsApiService
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
        val createdReservationId: Long? = null,
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
        Log.d("HotelVM", "HotelSharedViewModel 생성됨")
        Log.d("HotelVM", "초기 상태: ${_reservationState.value}")
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

    // 결제 준비 호출
    suspend fun preparePayment(reservationId: Long): PreparePaymentResponse? {
        return try {
            val res = paymentsApi.prepare(PreparePaymentRequest(reservationId))

            if (!res.isSuccessful) {
                _event.send("결제 준비 실패: HTTP ${res.code()}")
                return null
            }

            val body = res.body() // ApiResponse<PreparePaymentResponse>?
            if (body?.success == true && body.data != null) {
                body.data
            } else {
                _event.send(body?.message ?: "결제 준비 실패(빈 응답)")
                null
            }
        } catch (e: Throwable) {
            _event.send("결제 준비 오류: ${e.message}")
            null
        }
    }

    // 결제 검증 호출
    suspend fun verifyPayment(impUid: String, merchantUid: String): Boolean {
        return try {
            val res = paymentsApi.verify(VerifyPaymentRequest(merchantUid = merchantUid, impUid = impUid))
            Log.d("PAY viewMDoel", "verifyPayment:$merchantUid $impUid ")
            if (!res.isSuccessful) {
                _event.send("결제 검증 실패: HTTP ${res.code()}")
                return false
            }

            val body = res.body() // ApiResponse<VerifyPaymentResponse>?
            val ok = (body?.success == true && body.data?.confirmed == true)
            if (!ok) {
                _event.send(body?.data?.message ?: body?.message ?: "결제 검증 실패")
            }
            ok
        } catch (e: Throwable) {
            _event.send("결제 검증 오류: ${e.message}")
            false
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
            val fmt = DateTimeFormatter.ofPattern(pattern)
            var d = LocalDate.parse(checkIn, fmt)
            val end = LocalDate.parse(checkOut, fmt)

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


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun makeHotelReservation(): Long? {
        _uiState.update { it.copy(loading = true) }
        return try {
            val state = _reservationState.value
            val checkIn = state.checkInDate
            val checkOut = state.checkOutDate
            if (checkIn.isNullOrBlank() || checkOut.isNullOrBlank()) {
                _event.send("체크인/체크아웃 날짜를 선택해주세요.")
                null
            } else {
                val dates = buildStayDatesInclusive(checkIn, checkOut)
                if (dates.isEmpty()) {
                    _event.send("날짜 범위가 올바르지 않습니다.")
                    null
                } else {
                    val req = HotelReservationRequest(
//                        petId = state.selectedPetId, // 13 하드코딩 말고 상태값 사용 권장
                        petId = 13,
                        hotelId = state.selectedHotelId!!,
                        selectedDates = dates,
                        specialRequests = "",
                        checkInDate = checkIn,
                        checkOutDate = checkOut,
                        totalDays = dates.size,
                        consecutiveDates = true
                    )

                    val res = hotelApi.makeHotelReservation(req)
                    if (!res.isSuccessful) {
                        _event.send("호텔 예약 생성 실패 (${res.code()})")
                        null
                    } else {
                        // ⚠️ API 응답 스키마 확인 필요
                        // data가 Long이면: val reservationId = res.body()?.data
                        // data가 { id: Long, ... }면:
                        val reservationId = res.body()?.data?.id
                        if (reservationId == null) {
                            _event.send(res.body()?.message ?: "예약 생성 응답이 비어있습니다.")
                            null
                        } else {
                            _uiState.update { it.copy(createdReservationId = reservationId) }
                            reservationId
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            _event.send(t.message ?: "알 수 없는 오류")
            null
        } finally {
            _uiState.update { it.copy(loading = false) }
        }
    }



    suspend fun confirmReservation(reservationId: Long): Boolean = try {
        val res = hotelApi.confirmReservation(reservationId)
        val ok = (res.success == true)
        if (ok) {
            _uiState.update { it.copy(confirmed = true) }
            _event.send("예약이 확정되었습니다.")
        } else {
            _event.send("예약 확정 실패 (${res.success})")
        }
        ok
    } catch (t: Throwable) {
        _event.send(t.message ?: "예약 확정 중 오류")
        false
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




