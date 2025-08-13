package com.minjeok4go.petplace.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedTagJoin {
    private Long feedId;
    private Long tagId;
    private String tagName;
}
