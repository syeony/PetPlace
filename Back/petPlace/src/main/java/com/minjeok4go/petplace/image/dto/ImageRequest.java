package com.minjeok4go.petplace.image.dto;

import com.minjeok4go.petplace.common.constant.ImageType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageRequest {

    @NotNull
    private Long refId;

    @NotNull
    private ImageType refType;

    @NotNull
    private String src;

    @NotNull
    private Integer sort;

}
