package com.example.petplace.data.repository

import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.data.model.hotel.HotelDetail
import com.example.petplace.data.model.hotel.HotelDetailResponse
import com.example.petplace.data.model.hotel.HotelReservationRequest
import com.example.petplace.data.model.hotel.HotelSearchRequest
import com.example.petplace.data.model.hotel.HotelSearchResponse
import com.example.petplace.data.remote.HotelApiService
import com.example.petplace.presentation.feature.hotel.ApiResponse
import hilt_aggregated_deps._com_example_petplace_PetPlaceApp_GeneratedInjector
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.inject.Inject

class HotelRepository @Inject constructor(
    private val api: HotelApiService
) {
    suspend fun getHotelDetail(hotelId: Int): Result<HotelDetail> {
        return try {
            val res: Response<HotelDetailResponse> = api.getHotelDetail(hotelId)
            if (res.body()?.success == true) {
                Result.success(res.body()!!.data)
            } else {
                Result.failure(Exception(res.body()?.message?.ifBlank { "호텔 상세 조회 실패" } ?: "null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchHotel(request: HotelSearchRequest): Response<HotelSearchResponse> =
        api.searchHotel(request)

    suspend fun searchHotelWithAddress(address: String): Response<HotelSearchResponse> =
        api.searchHotelWithAddress(address)

    suspend fun checkReservationAvailability(
        request: CheckReservationAvailabilityRequest
    ) = api.checkReservationAvailability(request)

    suspend fun makeHotelReservation(
        request: HotelReservationRequest
    ) = api.makeHotelReservation(request)

    suspend fun confirmReservation(
        reservationId: Long
    ) = api.confirmReservation(reservationId)

}
