package com.minjeok4go.petplace.domain.feed.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDto {
    private Long id;
    private String content;
    private Long userId;            // ✅ uid → userId
    private String userNick;
    private String userImg;
    private Long regionId;          // ✅ rid → regionId
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer likes;          // ✅ like → likes
    private Integer views;          // ✅ view → views
    private Double score;           // ✅ 추천 점수
    private List<TagDto> tags;      // ✅ FeedTag → TagDto 변환
    private List<CommentDto> comments;
    private Integer commentCount;

    public static FeedDto from(Feed feed, Double score) {
        return FeedDto.builder()
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
                .score(score)
                .tags(feed.getFeedTags().stream()
                        .map(ft -> new TagDto(ft.getTag().getId(), ft.getTag().getName()))
                        .distinct()
                        .collect(Collectors.toList()))
                .comments(feed.getComments().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()))
                .commentCount(feed.getComments().size())
                .build();
    }
}
