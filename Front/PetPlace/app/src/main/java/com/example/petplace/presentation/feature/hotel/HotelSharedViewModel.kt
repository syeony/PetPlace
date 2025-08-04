package com.example.petplace.presentation.feature.hotel

import androidx.camera.video.AudioSpec.ChannelCount
import androidx.lifecycle.ViewModel
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

data class Hotel(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val pricePerNight: Int,
    val grade: Int,         // 별점 or 등급
    val imageUrl: String,
    val animalType: Int     // 1=강아지, 2=고양이, 3=둘 다
)

@HiltViewModel
class HotelSharedViewModel @Inject constructor() : ViewModel() {

    // 상태를 StateFlow로 관리
    private val _reservationState = MutableStateFlow(HotelReservationState())
    val reservationState: StateFlow<HotelReservationState> = _reservationState
    val current = _reservationState.value.animalCount
    fun selectAnimal(animal: String) {
        _reservationState.value = _reservationState.value.copy(selectedAnimal = animal)
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

    val sampleHotels = listOf(
        Hotel(
            id = 1,
            name = "펫팰리스 호텔",
            description = "반려동물 친화적인 5성급 호텔. 프리미엄 케어 서비스 제공",
            latitude = 36.1105,
            longitude = 128.4187,
            pricePerNight = 120000,
            grade = 5,
            imageUrl = "https://picsum.photos/400/200?random=1",
            animalType = 3
        ),
        Hotel(
            id = 2,
            name = "러브펫 하우스",
            description = "조용한 시골 감성의 펫 호텔, 넓은 산책 공간 제공",
            latitude = 36.1068,
            longitude = 128.4212,
            pricePerNight = 90000,
            grade = 4,
            imageUrl = "https://picsum.photos/400/200?random=2",
            animalType = 1
        ),
        Hotel(
            id = 3,
            name = "댕댕스테이",
            description = "중형견, 대형견 모두 환영! 야외 운동장이 있는 호텔",
            latitude = 36.1043,
            longitude = 128.4159,
            pricePerNight = 70000,
            grade = 3,
            imageUrl = "https://picsum.photos/400/200?random=3",
            animalType = 1
        ),
        Hotel(
            id = 4,
            name = "캣츠빌리지",
            description = "고양이 전용 호텔. 층별 캣타워와 개별 방 제공",
            latitude = 36.1091,
            longitude = 128.4125,
            pricePerNight = 80000,
            grade = 4,
            imageUrl = "https://picsum.photos/400/200?random=4",
            animalType = 2
        ),
        Hotel(
            id = 5,
            name = "펫라운지 호텔",
            description = "호텔과 카페가 함께 있는 공간. 보호자 대기 라운지 제공",
            latitude = 36.1112,
            longitude = 128.4163,
            pricePerNight = 100000,
            grade = 4,
            imageUrl = "https://picsum.photos/400/200?random=5",
            animalType = 3
        )
    )
}
