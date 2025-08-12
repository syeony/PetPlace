package com.example.petplace.data.remote

import com.example.petplace.data.model.payment.PreparePaymentRequest
import com.example.petplace.data.model.payment.PreparePaymentResponse
import com.example.petplace.data.model.payment.VerifyPaymentRequest
import com.example.petplace.data.model.payment.VerifyPaymentResponse
import com.example.petplace.presentation.feature.hotel.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface PaymentsApiService {
    @POST("/api/payments/prepare")
    suspend fun prepare(@Body req: PreparePaymentRequest): Response<ApiResponse<PreparePaymentResponse>>

    @POST("/api/payments/verify")
    suspend fun verify(
        @Body req: VerifyPaymentRequest
    ): Response<ApiResponse<VerifyPaymentResponse>>

}
