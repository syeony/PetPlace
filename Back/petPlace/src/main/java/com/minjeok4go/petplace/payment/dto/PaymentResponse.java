package com.minjeok4go.petplace.payment.dto;

import com.minjeok4go.petplace.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private String merchantUid;
    private String impUid;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private Payment.PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    private String failureReason;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reservationId(payment.getReservationId())
                .merchantUid(payment.getMerchantUid())
                .impUid(payment.getImpUid())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
