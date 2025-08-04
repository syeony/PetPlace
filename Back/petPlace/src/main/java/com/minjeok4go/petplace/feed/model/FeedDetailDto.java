package com.minjeok4go.petplace.feed.model;

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
    private Integer userId;            // ✅ uid → userId (타입 수정: Long → Integer)
    private String userNick;
    private String userImg;
    private Long regionId;          // ✅ rid → regionId
    private FeedCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer likes;          // ✅ like → likes
    private Integer views;          // ✅ view → views
    private List<TagDto> tags;
    private Integer commentCount;
    private List<CommentDto> comments;
}
