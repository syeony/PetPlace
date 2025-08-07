package com.minjeok4go.petplace.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class PortOneTokenResponse {
    private int code;
    private String message;
    private PortOneTokenData response;

    @Getter @Setter
    public static class PortOneTokenData {
        private String access_token;
        private int expired_at;
        private int now;

        public String getAccessToken() {
            return access_token;
        }
    }
}

