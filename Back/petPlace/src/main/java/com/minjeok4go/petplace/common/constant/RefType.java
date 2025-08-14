package com.minjeok4go.petplace.common.constant;

import lombok.Getter;

@Getter
public enum RefType {
    FEED("피드"),
    CARE("산책/돌봄"),
    HOTEL("호텔"),
    USER("펫 용품"),
    REVIEW("리뷰"),
    CHAT("채팅"),
    MISSING_REPORT("실종"),
    SIGHTING("목격");

    private final String displayName;
    RefType(String displayName){
        this.displayName = displayName;
    }
}
