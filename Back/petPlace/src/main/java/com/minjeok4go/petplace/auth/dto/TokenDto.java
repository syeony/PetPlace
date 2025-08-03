package com.minjeok4go.petplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;
    private String message;
    private UserInfo user;
    
    // 사용자 정보를 담는 내부 클래스
    @Getter
    @AllArgsConstructor
    public static class UserInfo {
        private String userName;
        private String nickname;
        private String userImgSrc;
        private Integer level;
        private Integer defaultPetId;
        private Long regionId;
    }
    
    // 기존 생성자 호환을 위한 정적 팩토리 메서드
    public static TokenDto of(String accessToken, String refreshToken, 
                              String userName, String nickname, String userImgSrc, 
                              Integer level, Integer defaultPetId, Long regionId) {
        UserInfo userInfo = new UserInfo(userName, nickname, userImgSrc, level, defaultPetId, regionId);
        return new TokenDto(accessToken, refreshToken, "로그인 성공", userInfo);
    }
}
