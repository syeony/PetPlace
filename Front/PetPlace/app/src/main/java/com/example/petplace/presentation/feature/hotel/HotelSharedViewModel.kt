package com.example.petplace.presentation.feature.hotel

import androidx.camera.video.AudioSpec.ChannelCount
import androidx.lifecycle.ViewModel
import com.example.petplace.data.model.hotel.HotelDetail
import com.example.petplace.data.model.hotel.HotelSearchResponse
import com.example.petplace.data.remote.HotelApiService
import com.example.petplace.data.remote.LoginApiService
import com.example.petplace.data.repository.HotelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

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

}
