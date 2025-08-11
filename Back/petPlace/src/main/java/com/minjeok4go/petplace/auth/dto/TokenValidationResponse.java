package com.minjeok4go.petplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "토큰 유효성 검증 응답 DTO")
public class TokenValidationResponse {

    @Schema(description = "토큰 유효 여부", example = "true")
    private boolean valid;

    @Schema(description = "검증 결과 메시지", example = "토큰이 유효합니다")
    private String message;

    @Schema(description = "토큰 소유자의 사용자 ID (유효할 경우)", example = "1")
    private Long userId;

    @Schema(description = "토큰의 남은 유효 시간(초 단위) (유효할 경우)", example = "3599")
    private Long expiresIn;

    public static TokenValidationResponse valid(Long userId, Long expiresIn) {
        return new TokenValidationResponse(true, "토큰이 유효합니다", userId, expiresIn);
    }

    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, message, null, null);
    }
}
