package com.minjeok4go.petplace.common.constant;

import lombok.Getter;

@Getter
public enum NotificationType {
    COMMENT("댓글"),
    SIGHT("목격"),
    LIKE("좋아요"),
    CHAT("채팅");

    private final String displayName;
    NotificationType(String displayName){
        this.displayName = displayName;
    }
}
