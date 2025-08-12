package com.minjeok4go.petplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 로그인 응답 DTO")
public class SocialLoginResponse {


    @Schema(description = "소셜 로그인 처리 결과 상태")
    private Status status;

    @Schema(description = "처리 결과 메시지", example = "로그인 성공")
    private String message;

    @Schema(description = "기존 사용자일 경우 발급되는 토큰 정보 (status가 EXISTING_USER일 때만 존재)")
    private TokenDto tokenDto;

    @Schema(description = "신규/연동 가능 사용자에게 발급되는 임시 토큰 (status가 NEW_USER 또는 LINKABLE_USER일 때 존재)", example = "eyJhbGciOiJIUzI1NiJ9.eyJ...")
    private String tempToken;

    @Schema(description = "연동 가능한 기존 계정의 사용자 ID (status가 LINKABLE_USER일 때만 존재)", example = "123")
    private Long linkableUserId;



    @Schema(description = "소셜 로그인 처리 상태 코드")
    public enum Status {
        @Schema(description = "기존 사용자 - 즉시 로그인 처리")
        EXISTING_USER,
        @Schema(description = "신규 사용자 - 본인인증 및 회원가입 필요")
        NEW_USER,
        @Schema(description = "연동 가능한 기존 계정 발견")
        LINKABLE_USER,
        @Schema(description = "처리 중 오류 발생")
        ERROR
    }

    // 정적 팩토리 메서드들
    public static SocialLoginResponse existingUser(TokenDto tokenDto) {
        return SocialLoginResponse.builder()
                .status(Status.EXISTING_USER)
                .message("로그인 성공")
                .tokenDto(tokenDto)
                .build();
    }

    public static SocialLoginResponse newUser(String tempToken) {
        return SocialLoginResponse.builder()
                .status(Status.NEW_USER)
                .message("본인인증이 필요합니다")
                .tempToken(tempToken)
                .build();
    }

    public static SocialLoginResponse linkableUser(Long userId, String tempToken) {
        return SocialLoginResponse.builder()
                .status(Status.LINKABLE_USER)
                .message("기존 계정과 연동이 가능합니다")
                .linkableUserId(userId)
                .tempToken(tempToken)
                .build();
    }

    public static SocialLoginResponse error(String message) {
        return SocialLoginResponse.builder()
                .status(Status.ERROR)
                .message(message)
                .build();
    }
}