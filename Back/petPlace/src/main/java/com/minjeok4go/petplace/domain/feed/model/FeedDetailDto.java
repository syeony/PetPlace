package com.minjeok4go.petplace.domain.feed.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedDetailDto {
    private Long id;
    private String content;
    private Long uid;
    private String userNick;
    private String userImg;
    private Long rid;
    private FeedCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer like;
    private Integer view;
    private List<TagDto> tags;
    private Integer commentCount;
    private List<CommentDto> comments;
}
