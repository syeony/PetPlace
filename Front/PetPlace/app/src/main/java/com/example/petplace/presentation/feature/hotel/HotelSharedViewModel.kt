package com.example.petplace.presentation.feature.hotel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.data.model.hotel.HotelDetail
import com.example.petplace.data.remote.HotelApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.hotel.HotelReservationRequest
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.model.payment.PaymentInfo
import com.example.petplace.data.model.payment.PreparePaymentRequest
import com.example.petplace.data.model.payment.PreparePaymentResponse
import com.example.petplace.data.remote.PaymentsApiService
import com.example.petplace.util.CommonUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

data class HotelReservationState(
    val selectedAnimal: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val selectedHotelId: Int? = null,
    val animalCount: Int? = 1,
    val selectedPetId: Int? = null,
    val specialRequest: String = "",
    val selectedPetType: String ="DOG"
)
data class PaymentCheckUiState(
    val loading: Boolean = false,
    val info: PaymentInfo? = null,
    val error: String? = null
)


@HiltViewModel
class HotelSharedViewModel @Inject constructor(
    private val hotelApi : HotelApiService,
    private val paymentsApi : PaymentsApiService,
    @ApplicationContext private val context: Context

) : ViewModel() {

    // 상태를 StateFlow로 관리
    private val _reservationState = MutableStateFlow(HotelReservationState())
    val reservationState: StateFlow<HotelReservationState> = _reservationState
    val current = _reservationState.value.animalCount



    fun fetchXY(context: Context) {
        viewModelScope.launch {
            val xy = CommonUtils.getXY(context)
            val location = CommonUtils.getCurrentLocation(context)
            Log.d("내 위치", "fetchXY: $location")
        }
    }


    private val _hotelList = MutableStateFlow<List<HotelDetail>>(emptyList())
    val hotelList: StateFlow<List<HotelDetail>> = _hotelList

    private val _myPetList = MutableStateFlow<List<MyPageInfoResponse.Pet>>(emptyList())
    val myPetList : StateFlow<List<MyPageInfoResponse.Pet>> = _myPetList


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

    private val _paymentUi = MutableStateFlow(PaymentCheckUiState())
    val paymentUi: StateFlow<PaymentCheckUiState> = _paymentUi.asStateFlow()

    fun updateSpecialRequest(text: String) {
        _reservationState.update { it.copy(specialRequest = text) }
    }

    private val _event = Channel<String>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    fun selecMyPet(petId:Int ,petType: String){
        _reservationState.value =_reservationState.value.copy(selectedPetId = petId, selectedPetType =petType )
    }
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

    fun pollPaymentUntilSettled(
        merchantUid: String,
        timeoutMs: Long = 120_000,
        intervalMs: Long = 2_000,
        onPaid: (PaymentInfo) -> Unit = {},
        onFinish: (PaymentInfo?, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            _paymentUi.value = PaymentCheckUiState(loading = true)

            while (System.currentTimeMillis() - start < timeoutMs) {
                val result = runCatching { paymentsApi.getPaymentInfo(merchantUid) }.getOrElse { err ->
                    _paymentUi.value = PaymentCheckUiState(loading = false, error = err.message)
                    onFinish(null, err.message)
                    return@launch
                }

                val info = result.body()?.data
                val ok = result.body()?.success
                val msg = result.body()?.message

                if (!ok!! || info == null) {
                    _paymentUi.value = PaymentCheckUiState(loading = false, error = msg ?: "조회 실패")
                    onFinish(null, msg ?: "조회 실패")
                    return@launch
                }

                _paymentUi.value = PaymentCheckUiState(loading = true, info = info)

                when (info.status.uppercase()) {
                    "PAID" -> {
                        _paymentUi.value = PaymentCheckUiState(loading = false, info = info)
                        onPaid(info)
                        onFinish(info, null)
                        return@launch
                    }
                    "FAILED", "CANCELED" -> {
                        _paymentUi.value = PaymentCheckUiState(loading = false, info = info, error = info.failureReason)
                        onFinish(info, info.failureReason ?: "결제 실패/취소")
                        return@launch
                    }
                }

                delay(intervalMs)
            }

            // 타임아웃
            val last = _paymentUi.value.info
            _paymentUi.value = _paymentUi.value.copy(loading = false)
            onFinish(last, "결제 확인이 지연됩니다. 잠시 후 다시 시도해 주세요.")
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

//    // 결제 검증 호출
//    suspend fun verifyPayment(impUid: String, merchantUid: String): Boolean {
//        return try {
//            val res = paymentsApi.verify(VerifyPaymentRequest(merchantUid = merchantUid, impUid = impUid))
//            Log.d("PAY viewMDoel", "verifyPayment:$merchantUid $impUid ")
//            if (!res.isSuccessful) {
//                _event.send("결제 검증 실패: HTTP ${res.code()}")
//                return false
//            }
//
//            val body = res.body() // ApiResponse<VerifyPaymentResponse>?
//            val ok = (body?.success == true && body.data?.confirmed == true)
//            if (!ok) {
//                _event.send(body?.data?.message ?: body?.message ?: "결제 검증 실패")
//            }
//            ok
//        } catch (e: Throwable) {
//            _event.send("결제 검증 오류: ${e.message}")
//            false
//        }
//    }




    suspend fun testWebhook(merchantUid: String, status: String): Boolean {
        val body = """{"merchantUid":"$merchantUid","status":"$status"}"""
        val res = paymentsApi.verifyWebhook(
            webhookId = "wh_${System.currentTimeMillis()}",
            webhookSignature = "computed-signature",
            webhookTimestamp = (System.currentTimeMillis()/1000).toString(),
            body = body
        )
        return res.isSuccessful
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

//    fun reset() {
//        _reservationState.value = HotelReservationState()
//    }

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

    suspend fun getHotelList(address: String = "구미") {
        try {
            fetchXY(context)

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


    // 내 펫 불러오기
    suspend fun getMyPets() {
        runCatching {
            hotelApi.getMyPets()
        }.onSuccess { res ->
            if (res.isSuccessful) {
                res.body()?.let { pets ->
                    _myPetList.value = pets
                } ?: run {
                    // body가 null인 경우 처리
                    _event.send("펫 정보가 없습니다.")
                }
            } else {
                _event.send("펫 정보 불러오기 실패 (${res.code()})")
            }
        }.onFailure { e ->
            _event.send("네트워크 오류: ${e.message}")
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
                        petId = state.selectedPetId!!, // 13 하드코딩 말고 상태값 사용 권장
//                        petId = 13,
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




