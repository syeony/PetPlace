package com.minjeok4go.petplace.domain.feed.model;

import lombok.Getter;

public enum FeedCategory {
    MYPET, SHARE, INFO, ANY, REVIEW
}

//@Getter
//public enum FeedCategory {
//    MY_PET("내 펫"),
//    SHARE("나눔"),
//    INFO("정보"),
//    ANY("자유"),
//    REVIEW("후기");
//
//    private final String displayName;
//    FeedCategory(String displayName){
//        this.displayName = displayName;
//    }
//}