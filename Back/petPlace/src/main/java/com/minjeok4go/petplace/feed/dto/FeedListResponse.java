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
public class FeedListResponse extends FeedDetailResponse {
    private Double score;
}
