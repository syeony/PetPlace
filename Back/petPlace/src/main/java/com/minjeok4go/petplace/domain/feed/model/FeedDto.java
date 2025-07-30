package com.minjeok4go.petplace.domain.feed.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDto {
    private Long id;
    private String content;
    private String userNick;
    private String userImg;
    private Integer rid;
    private String category;
    private List<Integer> tags;
    private LocalDateTime createdAt;
    private Integer like;
    private Integer view;
    private Double score; //  추천 점수 (선택 사항) (프런트에 추천 강도 표시, 추천 순서 정렬 등 필요할 때 사용하기 위해 추가)

    // Feed → FeedDto 변환 메서드 서비스 / 컨특롤러에서 쉽게 DTO로 변환하기 위해 쓰는 패턴 (정적 변환 메서드)
    public static FeedDto from(Feed feed, Double score) {
        return FeedDto.builder()
                .id(feed.getId())
                .content(feed.getContent())
                .userNick(feed.getUserNick())
                .userImg(feed.getUserImg())
                .rid(feed.getRid())
                .category(feed.getCategory())
                .tags(feed.getTags())
                .createdAt(feed.getCreatedAt())
                .like(feed.getLike())
                .view(feed.getView())
                .score(score)
                .build();
    }
}
