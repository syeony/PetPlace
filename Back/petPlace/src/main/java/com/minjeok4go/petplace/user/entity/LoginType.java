package com.minjeok4go.petplace.user.entity;

public enum LoginType {
    EMAIL("일반 이메일", false),
    KAKAO("카카오", true),
    //나중에 정유진이 더 추가할 시간이 생기면 다른 소셜타입도 고려할 예정
    NAVER("네이버", true),
    GOOGLE("구글", true);

    private final String displayName;
    private final boolean isSocial;

    LoginType(String displayName, boolean isSocial) {
        this.displayName = displayName;
        this.isSocial = isSocial;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSocial() {
        return isSocial;
    }
}