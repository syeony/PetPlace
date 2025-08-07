package com.minjeok4go.petplace.auth.dto;

import com.minjeok4go.petplace.auth.dto.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginResponse {

    public enum Status {
        EXISTING_USER,      // 기존 사용자 - 바로 로그인
        NEW_USER,          // 신규 사용자 - 본인인증 필요
        LINKABLE_USER,     // 연동 가능한 기존 계정 존재
        ERROR              // 오류 발생
    }

    private Status status;
    private String message;
    private TokenDto tokenDto;          // 기존 사용자일 경우
    private String tempToken;           // 신규 사용자용 임시 토큰
    private Long linkableUserId;        // 연동 가능한 사용자 ID

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