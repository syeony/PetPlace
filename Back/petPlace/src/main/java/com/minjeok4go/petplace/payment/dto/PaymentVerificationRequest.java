package com.minjeok4go.petplace.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentVerificationRequest {

    @NotBlank(message = "주문번호는 필수입니다.")
    private String merchantUid;

    @NotBlank(message = "포트원 거래번호는 필수입니다.")
    private String impUid;
}
