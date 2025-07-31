package com.minjeok4go.petplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
    private String message;
    private boolean success;

    public static TokenRefreshResponseDto success(String accessToken, String refreshToken) {
        return TokenRefreshResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("토큰 갱신 성공")
                .success(true)
                .build();
    }

    public static TokenRefreshResponseDto failure(String message) {
        return TokenRefreshResponseDto.builder()
                .message(message)
                .success(false)
                .build();
    }
}
