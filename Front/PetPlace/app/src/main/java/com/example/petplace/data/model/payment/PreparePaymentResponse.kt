package com.example.petplace.data.model.payment

data class PreparePaymentRequest(val reservationId: Long)
data class PreparePaymentResponse(
    val merchantUid: String,
    val amount: Int
)
data class VerifyPaymentRequest(
    val merchantUid: String,
    val impUid: String
)
data class VerifyPaymentResponse(
    val confirmed: Boolean,
    val message: String?
)