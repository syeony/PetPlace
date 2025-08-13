package com.example.petplace.data.model.payment

import com.google.gson.annotations.SerializedName

data class PreparePaymentRequest(val reservationId: Long)
data class PreparePaymentResponse(
    val merchantUid: String,
    val amount: Int
)
//data class VerifyPaymentRequest(
//    val merchantUid: String,
//    val impUid: String
//)

data class VerifyPaymentRequest(
    // 헤더
    val webhookId: String,
    val webhookSignature: String,
    val webhookTimestamp: String,

    // 바디 - 실제 결제 데이터 (예시)
    val impUid: String?,         // 아임포트 결제 고유번호
    val merchantUid: String?,    // 가맹점 주문번호
    val status: String?,         // 결제 상태 (paid, failed 등)
    val amount: Int?,            // 결제 금액
    val currency: String?,       // 통화 단위
    val paidAt: String?,         // 결제 완료 시각
    val customData: String?      // 커스텀 데이터(있다면)
)


data class VerifyPaymentResponse(
    val confirmed: Boolean,
    val message: String?
)
data class PaymentInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("reservationId") val reservationId: Int,
    @SerializedName("merchantUid") val merchantUid: String,
    @SerializedName("impUid") val impUid: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("status") val status: String,          // ex) PAID / FAILED / PENDING ...
    @SerializedName("paymentMethod") val paymentMethod: String, // ex) CARD / KAKAOPAY ...
    @SerializedName("paidAt") val paidAt: String?,         // ISO-8601, 필요시 어댑터로 Date 변환
    @SerializedName("failureReason") val failureReason: String?
)