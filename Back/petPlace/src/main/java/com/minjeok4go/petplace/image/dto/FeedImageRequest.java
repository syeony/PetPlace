package com.minjeok4go.petplace.image.dto;

import com.minjeok4go.petplace.common.constant.ImageType;
import jakarta.persistence.Column;
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
