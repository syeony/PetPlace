package com.minjeok4go.petplace.common.constant;

import lombok.Getter;

@Getter
public enum ActivityType {
    FEED_CREATE(+10),
    FEED_DELETE(-10),

    COMMENT_CREATE(+4),
    COMMENT_DELETE(-4),

    LIKE_CREATE(+1),
    LIKE_DELETE(-1);

    private final int expDelta;

    ActivityType(int expDelta) { this.expDelta = expDelta; }
}