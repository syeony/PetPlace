package com.minjeok4go.petplace.payment.dto;

import com.minjeok4go.petplace.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "결제 정보 응답 DTO")
public class PaymentResponse {
    @Schema(description = "결제 고유 ID", example = "201")
    private Long id;
    @Schema(description = "연결된 예약 ID", example = "101")
    private Long reservationId;
    @Schema(description = "가맹점 주문번호", example = "petplace_1723123456789")
    private String merchantUid;
    @Schema(description = "포트원 결제번호", example = "imp_123456789012")
    private String impUid;
    @Schema(description = "결제 금액", example = "300000.00")
    private BigDecimal amount;
    @Schema(description = "결제 상태", example = "PAID")
    private Payment.PaymentStatus status;
    @Schema(description = "결제 수단", example = "CARD")
    private Payment.PaymentMethod paymentMethod;
    @Schema(description = "결제 완료 시각", example = "2024-08-15T14:30:00")
    private LocalDateTime paidAt;
    @Schema(description = "결제 실패 사유 (실패 시에만 존재)", example = "한도 초과")
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
