package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.comment.dto.FeedComment;
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
public class FeedDetailResponse {
    private Long id;
    private String content;
    private Long userId;
    private String userNick;
    private String userImg;
    private Long regionId;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean liked;
    private Integer likes;
    private Integer views;
    private List<TagResponse> tags;
    private List<ImageResponse> images;
    private Integer commentCount;
    private List<FeedComment> comments;
}
