package com.minjeok4go.petplace.feed.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedLikeResponse {

    @NotNull
    private Long feedId;

    @NotNull
    private Integer feedLikes;
}
