package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedListResponse {
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
    private Integer likes;
    private Integer views;
    private Double score;
    private List<TagResponse> tags;
    private List<ImageResponse> images;
    private List<FeedComment> comments;
    private Integer commentCount;
}
