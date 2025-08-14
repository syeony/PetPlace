package com.example.petplace.data.remote

import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.model.chat.CreateChatRoomRequest
import com.example.petplace.data.model.feed.FeedRecommendRes
import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.data.model.hotel.HotelDetailResponse
import com.example.petplace.data.model.hotel.HotelReservationRequest
import com.example.petplace.data.model.hotel.HotelSearchRequest
import com.example.petplace.data.model.hotel.HotelSearchResponse
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.presentation.feature.hotel.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HotelApiService {

    @GET("/api/hotels/{hotelId}")
    suspend fun getHotelDetail(
        @Path("hotelId") hotelId: Int
    ): Response<HotelDetailResponse>

    @POST("/api/hotels/search")
    suspend fun searchHotel(
        @Body request: HotelSearchRequest
    ): Response<HotelSearchResponse> // 그대로 사용

    // 주소로 검색 — 서버가 리스트로 주면 그대로 둠
    @GET("/api/hotels/search/address")
    suspend fun searchHotelWithAddress(
        @Query("address") address: String
    ):  Response<HotelSearchResponse>

    @POST("/api/reservations/check-availability")
    suspend fun checkReservationAvailability(
        @Body request: CheckReservationAvailabilityRequest
    ): Response<ApiResponse<Boolean>>

    @POST("/api/reservations")
    suspend fun makeHotelReservation(
        @Body request: HotelReservationRequest
    ): Response<ApiResponse<ReservationResponse>>

    @PUT("/api/reservations/{reservationId}/confirm")
    suspend fun confirmReservation(
        @Path("reservationId") reservationId: Long
    ): ApiResponse<Unit?>
    @GET("/api/pets/me")
    suspend fun getMyPets(): Response<List<MyPageInfoResponse.Pet>>


}// 예약 생성 응답 DTO
data class ReservationResponse(
    val id: Long,
    val userId: Int,
    val petId: Int,
    val hotelId: Int,
    val hotelName: String,
    val reservedDates: List<String>,
    val checkInDate: String,
    val checkOutDate: String,
    val totalDays: Int,
    val totalPrice: Int,
    val status: String,
    val specialRequests: String?,
    val createdAt: String,
    val consecutiveReservation: Boolean,
    val reservationPeriod: String
)
