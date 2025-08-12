package com.minjeok4go.petplace.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class PortoneV2PaymentResponse {
    @JsonProperty("id")
    private String id;                    // paymentId

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("storeId")
    private String storeId;

    @JsonProperty("method")
    private PaymentMethodInfo method;

    @JsonProperty("channel")
    private ChannelInfo channel;

    @JsonProperty("version")
    private String version;

    @JsonProperty("scheduleId")
    private String scheduleId;

    @JsonProperty("billingKey")
    private String billingKey;

    @JsonProperty("webhooks")
    private WebhookInfo[] webhooks;

    @JsonProperty("requestedAt")
    private String requestedAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("statusChangedAt")
    private String statusChangedAt;

    @JsonProperty("orderName")
    private String orderName;

    @JsonProperty("amount")
    private AmountInfo amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("customer")
    private CustomerInfo customer;

    @JsonProperty("customData")
    private String customData;

    @JsonProperty("status")
    private String status;  // READY, PENDING, VIRTUAL_ACCOUNT_ISSUED, PAID, CANCELLED, PARTIAL_CANCELLED, FAILED

    // 내부 클래스들
    @Getter @Setter
    public static class AmountInfo {
        private Long total;
        private Long taxFree;
        private Long discount;
        private Long paid;
        private Long cancelled;
        private Long cancellable;
    }

    @Getter @Setter
    public static class CustomerInfo {
        private String id;
        private String name;
        private String email;
        private String phoneNumber;
    }

    @Getter @Setter
    public static class PaymentMethodInfo {
        private String type;
        private CardInfo card;
        private EasyPayInfo easyPay;
    }

    @Getter @Setter
    public static class CardInfo {
        private String company;
        private String number;
        private String installmentMonth;
        private String approvalNumber;
    }

    @Getter @Setter
    public static class EasyPayInfo {
        private String provider;
        private String method;
    }

    @Getter @Setter
    public static class ChannelInfo {
        private String type;
        private String id;
        private String key;
        private String name;
        private String pgProvider;
        private String pgMerchantId;
    }

    @Getter @Setter
    public static class WebhookInfo {
        private String id;
        private String status;
        private String url;
    }
}