package com.minjeok4go.petplace.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결제 준비 요청 DTO")
public class PaymentPrepareRequest {

    @Schema(description = "결제를 진행할 예약의 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 ID는 필수입니다.")
    private Long reservationId;
}
