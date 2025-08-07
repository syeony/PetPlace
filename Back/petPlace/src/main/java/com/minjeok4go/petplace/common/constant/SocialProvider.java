package com.minjeok4go.petplace.common.constant;

public enum SocialProvider {
    KAKAO("카카오", "kakao_"),
    NAVER("네이버", "naver_"),
    GOOGLE("구글", "google_");

    private final String displayName;
    private final String userNamePrefix;

    SocialProvider(String displayName, String userNamePrefix) {
        this.displayName = displayName;
        this.userNamePrefix = userNamePrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserNamePrefix() {
        return userNamePrefix;
    }

    /**
     * 소셜 ID로 userName 생성
     * @param socialId 소셜 플랫폼의 고유 ID
     * @return 생성된 userName (예: "kakao_12345678")
     */
    public String createUserName(String socialId) {
        return userNamePrefix + socialId;
    }
}