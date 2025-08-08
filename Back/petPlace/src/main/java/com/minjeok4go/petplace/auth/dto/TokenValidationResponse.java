package com.minjeok4go.petplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean valid;
    private String message;
    private Long userId;        // 토큰이 유효할 때만 반환
    private Long expiresIn;     // 남은 유효시간(초)

    // 토큰이 유효한 경우
    public static TokenValidationResponse valid(Long userId, Long expiresIn) {
        return new TokenValidationResponse(true, "토큰이 유효합니다", userId, expiresIn);
    }

    // 토큰이 유효하지 않은 경우
    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, message, null, null);
    }
}
