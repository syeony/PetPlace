package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.common.constant.FeedCategory;
import com.minjeok4go.petplace.image.dto.ImageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFeedResponse {
    private Long id;
}
