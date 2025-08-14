
package com.minjeok4go.petplace.feed.dto;
import com.minjeok4go.petplace.feed.entity.Feed;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedListResponse extends FeedDetailResponse {
    private Double score;

    // [추천] 댓글 수를 같이 세팅하는 오버로드
    public static FeedListResponse from(Feed feed, double score, int commentCount) {
        return FeedListResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory().getDisplayName())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .likes(feed.getLikes())
                .views(feed.getViews())
                .score(score)
                .commentCount(commentCount)
                .build();
    }


}