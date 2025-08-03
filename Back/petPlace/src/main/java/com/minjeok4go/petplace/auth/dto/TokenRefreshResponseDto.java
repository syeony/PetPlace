package com.minjeok4go.petplace.auth.dto;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
    private String message;
    private boolean success;
    //Builder에서 생성자 사용으로 변경 함
    public static TokenRefreshResponseDto success(String accessToken, String refreshToken) {
        return new TokenRefreshResponseDto(  // 생성자 사용
                accessToken,
                refreshToken,
                "토큰 갱신 성공",
                true
        );
    }

    public static TokenRefreshResponseDto failure(String message) {
        return new TokenRefreshResponseDto(null, null, message, false);
    }

}
