package com.minjeok4go.petplace.image.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedImageRequest {

    @NotNull
    private String src;

    @NotNull
    private Integer sort;
}
