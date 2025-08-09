package com.minjeok4go.petplace.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결제 검증 요청 DTO")
public class PaymentVerificationRequest {

    @Schema(description = "가맹점에서 생성한 고유 주문번호", example = "petplace_1723123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "주문번호는 필수입니다.")
    private String merchantUid;

    @Schema(description = "포트원에서 발급한 고유 결제번호", example = "imp_123456789012", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "포트원 거래번호는 필수입니다.")
    private String impUid;
}

