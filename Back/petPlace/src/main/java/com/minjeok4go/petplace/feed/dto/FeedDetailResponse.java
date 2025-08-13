package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.comment.dto.FeedComment;
import com.minjeok4go.petplace.comment.entity.Comment;
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
@SuperBuilder(toBuilder = true)
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

    // ✔ 여기만 카운트 필드로 유지
    @Builder.Default
    private int commentCount = 0;

    private List<FeedComment> comments;

    public FeedDetailResponse(
            Feed feed, boolean liked,
            List<TagResponse> tags, List<ImageResponse> images,
            List<Comment> comments, List<FeedComment> commentDtos
    ) {
        this.id = feed.getId();
        this.content = feed.getContent();
        this.userId = feed.getUserId();
        this.userNick = feed.getUserNick();
        this.userImg = feed.getUserImg();
        this.regionId = feed.getRegionId();
        this.category = feed.getCategory() != null ? feed.getCategory().getDisplayName() : null;
        this.createdAt = feed.getCreatedAt();
        this.updatedAt = feed.getUpdatedAt();
        this.deletedAt = feed.getDeletedAt();
        this.liked = liked;
        this.likes = feed.getLikes();
        this.views = feed.getViews();

        this.tags = tags;
        this.images = images;

        this.comments = commentDtos;

        // 초기값(서비스에서 실제 카운트로 덮어씀)
        this.commentCount = (comments != null) ? comments.size() : 0;
    }
}

