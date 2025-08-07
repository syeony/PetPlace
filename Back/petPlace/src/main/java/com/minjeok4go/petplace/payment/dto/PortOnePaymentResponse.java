package com.minjeok4go.petplace.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class PortOnePaymentResponse {
    private int code;
    private String message;
    private PortOnePaymentData response;

    @Getter @Setter
    public static class PortOnePaymentData {
        private String imp_uid;
        private String merchant_uid;
        private String pay_method;
        private String channel;
        private String pg_provider;
        private String emb_pg_provider;
        private String pg_tid;
        private String pg_id;
        private boolean escrow;
        private String apply_num;
        private String bank_code;
        private String bank_name;
        private String card_code;
        private String card_name;
        private int card_quota;
        private String card_number;
        private String card_type;
        private String vbank_code;
        private String vbank_name;
        private String vbank_num;
        private String vbank_holder;
        private Long vbank_date;
        private String vbank_issued_at;
        private String name;
        private BigDecimal amount;
        private BigDecimal cancel_amount;
        private String currency;
        private String buyer_name;
        private String buyer_email;
        private String buyer_tel;
        private String buyer_addr;
        private String buyer_postcode;
        private String custom_data;
        private String user_agent;
        private String status;
        private Long started_at;
        private Long paid_at;
        private Long failed_at;
        private Long cancelled_at;
        private String fail_reason;
        private String cancel_reason;
        private String receipt_url;

        public String getMerchantUid() {
            return merchant_uid;
        }

        public String getImpUid() {
            return imp_uid;
        }
    }
}
