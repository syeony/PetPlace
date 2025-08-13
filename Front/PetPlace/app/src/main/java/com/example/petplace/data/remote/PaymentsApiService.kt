package com.example.petplace.data.remote

import com.example.petplace.data.model.payment.PaymentInfo
import com.example.petplace.data.model.payment.PreparePaymentRequest
import com.example.petplace.data.model.payment.PreparePaymentResponse
import com.example.petplace.data.model.payment.VerifyPaymentRequest
import com.example.petplace.data.model.payment.VerifyPaymentResponse
import com.example.petplace.presentation.feature.hotel.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface PaymentsApiService {
    @POST("/api/payments/prepare")
    suspend fun prepare(@Body req: PreparePaymentRequest): Response<ApiResponse<PreparePaymentResponse>>

//    @POST("/api/payments/verify")
//    suspend fun verify(
//        @Body req: VerifyPaymentRequest
//    ): Response<ApiResponse<VerifyPaymentResponse>>

    @POST("/api/payments/webhook")
    suspend fun verifyWebhook(
        @Header("webhook-id") webhookId: String,
        @Header("webhook-signature") webhookSignature: String,
        @Header("webhook-timestamp") webhookTimestamp: String,
        @Body body: String
    ): Response<Unit>

    @GET("/api/payments/{merchantUid}")
    suspend fun getPaymentInfo(
        @Path("merchantUid") merchantUid: String
    ): Response<ApiResponse<PaymentInfo>>
}
