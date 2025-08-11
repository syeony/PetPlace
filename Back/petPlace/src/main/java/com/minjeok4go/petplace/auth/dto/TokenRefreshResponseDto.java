package com.minjeok4go.petplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 갱신 응답 DTO")
public class TokenRefreshResponseDto {

    @Schema(description = "새로 발급된 Access Token")
    private String accessToken;

    @Schema(description = "새로 발급된 Refresh Token (정책에 따라 변경될 수 있음)")
    private String refreshToken;

    @Schema(description = "응답 메시지", example = "토큰 갱신 성공")
    private String message;

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    public static TokenRefreshResponseDto success(String accessToken, String refreshToken) {
        return new TokenRefreshResponseDto(accessToken, refreshToken, "토큰 갱신 성공", true);
    }

    public static TokenRefreshResponseDto failure(String message) {
        return new TokenRefreshResponseDto(null, null, message, false);
    }
}