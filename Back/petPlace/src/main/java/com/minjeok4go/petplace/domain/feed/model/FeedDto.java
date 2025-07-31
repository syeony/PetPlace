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
    private Long uid;
    private String userNick;
    private String userImg;
    private Long rid;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer like;
    private Integer view;
    private Double score;  // ✅ 추천 점수 추가
    private List<TagDto> tags;  // ✅ 태그 객체 리스트로 변경
    private List<CommentDto> comments;
    private Integer commentCount;

    public static FeedDto from(Feed feed, Double score) {
        return FeedDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .uid(feed.getUid())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .rid(feed.getRid())
                .category(feed.getCategory().name())
                .createdAt(feed.getCreatedAt())
                .updatedAt(feed.getUpdatedAt())
                .deletedAt(feed.getDeletedAt())
                .like(feed.getLike())
                .view(feed.getView())
                .score(score)  // ✅ 추천 점수 설정
                .tags(feed.getHashtags().stream()
                        .map(h -> new TagDto(h.getTag().getId(), h.getTag().getTagName()))
                        .distinct()
                        .collect(Collectors.toList()))
                .comments(feed.getComments().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()))
                .commentCount(feed.getComments().size())
                .build();
    }
}
