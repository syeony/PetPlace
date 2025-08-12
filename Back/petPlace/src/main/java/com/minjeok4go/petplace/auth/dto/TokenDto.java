package com.minjeok4go.petplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 및 토큰 발급 응답 DTO")
public class TokenDto {
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN...")
    private String accessToken;

    @Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2...")
    private String refreshToken;

    @Schema(description = "응답 메시지", example = "로그인 성공")
    private String message;

    @Schema(description = "로그인한 사용자 정보")
    private UserInfo user;

    @Getter
    @AllArgsConstructor
    @Schema(description = "로그인한 사용자의 상세 정보")
    public static class UserInfo {
        @Schema(description = "사용자 고유 ID", example = "1")
        private Long userId;

        @Schema(description = "사용자 아이디", example = "petlover123")
        private String userName;

        @Schema(description = "사용자 닉네임", example = "멍멍이아빠")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String userImgSrc;

        @Schema(description = "사용자 레벨", example = "3")
        private Integer level;

        @Schema(description = "대표 반려견 ID", example = "5")
        private Integer defaultPetId;

        @Schema(description = "활동 지역 ID", example = "11")
        private Long regionId;

        @Schema(description = "전화번호", example = "010-1234-5678")
        private String phoneNumber;
    }

    
    // 기존 생성자 호환을 위한 정적 팩토리 메서드
    public static TokenDto of(String accessToken, String refreshToken, Long userId,
                              String userName, String nickname, String userImgSrc, 
                              Integer level, Integer defaultPetId, Long regionId, String phoneNumber) {
        UserInfo userInfo = new UserInfo(userId, userName, nickname, userImgSrc, level, defaultPetId, regionId,phoneNumber);
        return new TokenDto(accessToken, refreshToken, "로그인 성공", userInfo);
    }
}
