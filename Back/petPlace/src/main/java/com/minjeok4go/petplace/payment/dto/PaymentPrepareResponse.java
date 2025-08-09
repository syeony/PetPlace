package com.minjeok4go.petplace.payment.dto;

import com.minjeok4go.petplace.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "결제 준비 응답 DTO")
public class PaymentPrepareResponse {

    @Schema(description = "가맹점에서 생성한 고유 주문번호", example = "petplace_1723123456789")
    private String merchantUid;

    @Schema(description = "결제될 총 금액", example = "300000.00")
    private BigDecimal amount;

    public static PaymentPrepareResponse from(Payment payment) {
        return PaymentPrepareResponse.builder()
                .merchantUid(payment.getMerchantUid())
                .amount(payment.getAmount())
                .build();
    }
}
