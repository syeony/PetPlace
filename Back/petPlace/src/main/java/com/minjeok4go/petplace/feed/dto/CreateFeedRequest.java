package com.minjeok4go.petplace.feed.dto;

import com.minjeok4go.petplace.image.dto.FeedImageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedRequest {

    @NotBlank
    private String content;

    @NotNull
    private Long regionId;

    @NotNull
    private String category;

    @Builder.Default
    private List<Long> tagIds = new ArrayList<>();

    @Builder.Default
    private List<FeedImageRequest> images = new ArrayList<>();
}
