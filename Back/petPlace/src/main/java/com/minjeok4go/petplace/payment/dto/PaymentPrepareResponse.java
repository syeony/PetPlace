package com.minjeok4go.petplace.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class PaymentPrepareResponse {
    private String merchantUid;
    private BigDecimal amount;
}
