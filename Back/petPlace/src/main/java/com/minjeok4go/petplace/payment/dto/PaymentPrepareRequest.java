package com.minjeok4go.petplace.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentPrepareRequest {

    @NotNull(message = "예약 ID는 필수입니다.")
    private Long reservationId;
}
