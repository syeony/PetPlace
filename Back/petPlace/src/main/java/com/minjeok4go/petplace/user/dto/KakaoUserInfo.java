package com.minjeok4go.petplace.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 API에서 받은 사용자 정보를 담는 DTO
 * GET https://kapi.kakao.com/v2/user/me 응답 구조
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserInfo {
    
    private Long id; // 카카오 고유 ID
    
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    
    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoAccount {
        private String email;
        
        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;
        
        @JsonProperty("has_email")
        private Boolean hasEmail;
        
        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;
        
        @JsonProperty("profile")
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Profile {
            private String nickname;
            
            @JsonProperty("profile_image_url")
            private String profileImageUrl;
            
            @JsonProperty("thumbnail_image_url")
            private String thumbnailImageUrl;
        }
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {
        private String nickname;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }
    
    // 편의 메서드들
    public String getSocialId() {
        return id != null ? id.toString() : null;
    }
    
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.getEmail() : null;
    }
    
    public String getNickname() {
        // 프로필 닉네임 우선, 없으면 properties 닉네임
        if (kakaoAccount != null && kakaoAccount.getProfile() != null && 
            kakaoAccount.getProfile().getNickname() != null) {
            return kakaoAccount.getProfile().getNickname();
        }
        return properties != null ? properties.getNickname() : null;
    }
    
    public String getProfileImage() {
        // 프로필 이미지 우선, 없으면 properties 이미지
        if (kakaoAccount != null && kakaoAccount.getProfile() != null && 
            kakaoAccount.getProfile().getProfileImageUrl() != null) {
            return kakaoAccount.getProfile().getProfileImageUrl();
        }
        return properties != null ? properties.getProfileImage() : null;
    }
}
