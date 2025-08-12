package com.example.petplace.data.repository

import com.example.petplace.data.model.hotel.CheckReservationAvailabilityRequest
import com.example.petplace.data.model.hotel.HotelDetail
import com.example.petplace.data.model.hotel.HotelDetailResponse
import com.example.petplace.data.model.hotel.HotelReservationRequest
import com.example.petplace.data.model.hotel.HotelSearchRequest
import com.example.petplace.data.model.hotel.HotelSearchResponse
import com.example.petplace.data.model.payment.PreparePaymentRequest
import com.example.petplace.data.model.payment.PreparePaymentResponse
import com.example.petplace.data.model.payment.VerifyPaymentRequest
import com.example.petplace.data.model.payment.VerifyPaymentResponse
import com.example.petplace.data.remote.HotelApiService
import com.example.petplace.data.remote.PaymentsApiService
import com.example.petplace.presentation.feature.hotel.ApiResponse
import com.google.android.gms.common.api.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

class PyamentRepository @Inject constructor(
    private val api: PaymentsApiService
) {
    suspend fun prepare( req: PreparePaymentRequest)= api.prepare(req)

    suspend fun verify( req: VerifyPaymentRequest)= api.verify(req)

}

