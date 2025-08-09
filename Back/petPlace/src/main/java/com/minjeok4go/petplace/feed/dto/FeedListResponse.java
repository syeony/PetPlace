
package com.minjeok4go.petplace.feed.dto;
import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.common.constant.FeedCategory;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedListResponse extends FeedDetailResponse {
    private Double score;
    // FeedListResponse.java (DTO에 추가)
    public static FeedListResponse from(Feed feed, Double score) {
        return FeedListResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory().name())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .likes(feed.getLikes())
                .views(feed.getViews())
                // ... 필요한 필드 추가
                .score(score)
                .build();
    }
    public static FeedListResponse from(Feed feed) {
        return FeedListResponse.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userId(feed.getUserId())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .regionId(feed.getRegionId())
                .category(feed.getCategory().name())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .likes(feed.getLikes())
                .views(feed.getViews())
                // 이미 fetch join으로 User와 Pet을 불러왔다면 여기서 바로 사용 가능
                // pets 정보를 DTO로 변환해서 넣고 싶다면 아래 예시처럼
                // .pets(feed.getUser().getPets().stream()
                //        .map(PetListResponse::from)
                //        .collect(Collectors.toList()))
                .build();
    }
}