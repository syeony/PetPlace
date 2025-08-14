package com.minjeok4go.petplace.common.constant;

import lombok.Getter;

@Getter
public enum FeedCategory {
    MYPET("내새꾸자랑"),
    SHARE("나눔"),
    INFO("정보"),
    ANY("자유"),
    REVIEW("후기");

    private final String displayName;
    FeedCategory(String displayName){
        this.displayName = displayName;
    }
}