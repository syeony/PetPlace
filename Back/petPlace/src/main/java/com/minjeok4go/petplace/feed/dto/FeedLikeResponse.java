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
//
//    @NotNull
//    private Integer feedLikes;

    private boolean liked;   // 현재 내가 좋아요한 상태
    private int likeCount;   // 최종 카운트

}
