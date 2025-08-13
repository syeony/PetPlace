package com.minjeok4go.petplace.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PortoneWebhookRequest {
    private String type; // Transaction.Paid, Transaction.Cancelled 등
    private Long timestamp;
    private PaymentData data;

    @Getter @Setter
    public static class PaymentData {
        @JsonProperty("paymentId")
        private String paymentId;        // 결제 고유 ID (V2에서는 paymentId 사용)

        @JsonProperty("transactionId")
        private String transactionId;    // 거래 ID

        @JsonProperty("merchantId")
        private String merchantId;       // 가맹점 ID

        @JsonProperty("storeId")
        private String storeId;         // 상점 ID

        @JsonProperty("method")
        private PaymentMethod method;    // 결제 수단

        @JsonProperty("channel")
        private PaymentChannel channel;  // 결제 채널

        @JsonProperty("version")
        private String version;         // API 버전

        @JsonProperty("requestedAt")
        private String requestedAt;     // 결제 요청 시각

        @JsonProperty("updatedAt")
        private String updatedAt;       // 결제 상태 업데이트 시각

        @JsonProperty("statusChangedAt")
        private String statusChangedAt; // 상태 변경 시각

        @JsonProperty("orderName")
        private String orderName;       // 주문명

        @JsonProperty("amount")
        private AmountData amount;      // 금액 정보

        @JsonProperty("currency")
        private String currency;        // 통화

        @JsonProperty("customer")
        private CustomerData customer;  // 고객 정보

        @JsonProperty("customData")
        private String customData;      // 커스텀 데이터
    }

    @Getter @Setter
    public static class AmountData {
        private Long total;       // 총 결제 금액
        private Long taxFree;     // 면세 금액
        private Long discount;    // 할인 금액
        private Long paid;        // 실제 결제된 금액
        private Long cancelled;   // 취소된 금액
        private Long cancellable; // 취소 가능 금액
    }

    @Getter @Setter
    public static class CustomerData {
        private String customerId;
        private String customerName;
        private String customerEmail;
        private String customerPhoneNumber;
    }

    @Getter @Setter
    public static class PaymentMethod {
        private String type; // CARD, EASY_PAY 등
        private PaymentMethodCard card;
        private PaymentMethodEasyPay easyPay;
    }

    @Getter @Setter
    public static class PaymentMethodCard {
        private String company;
        private String number;
        private String installmentMonth;
        private String approvalNumber;
    }

    @Getter @Setter
    public static class PaymentMethodEasyPay {
        private String provider; // KAKAOPAY, NAVERPAY 등
        private String method;
    }

    @Getter @Setter
    public static class PaymentChannel {
        private String type;
        private String id;
        private String key;
        private String name;
        private String pgProvider;
        private String pgMerchantId;
    }
}