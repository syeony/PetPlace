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
        private String userId;
        private String nickname;
        private String userImgSrc;
        private Integer level;
        private Long defaultPetId;
        private Long rid;
    }
    
    // 기존 생성자 호환을 위한 정적 팩토리 메서드
    public static TokenDto of(String accessToken, String refreshToken, 
                              String userId, String nickname, String userImgSrc, 
                              Integer level, Long defaultPetId, Long rid) {
        UserInfo userInfo = new UserInfo(userId, nickname, userImgSrc, level, defaultPetId, rid);
        return new TokenDto(accessToken, refreshToken, "로그인 성공", userInfo);
    }
}
