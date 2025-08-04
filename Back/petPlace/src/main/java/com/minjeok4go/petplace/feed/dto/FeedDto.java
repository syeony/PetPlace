package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.comment.dto.CommentDto;
import com.minjeok4go.petplace.common.constant.ImageType;
import com.minjeok4go.petplace.feed.entity.Feed;
import com.minjeok4go.petplace.image.dto.ImageResponse;
import com.minjeok4go.petplace.image.repository.ImageRepository;
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
    private Integer userId;
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
    private List<TagDto> tags;
    private List<ImageResponse> images;
    private List<CommentDto> comments;
    private Integer commentCount;
}
