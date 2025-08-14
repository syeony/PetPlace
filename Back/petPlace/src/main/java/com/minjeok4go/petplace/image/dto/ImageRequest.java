package com.minjeok4go.petplace.image.dto;

import com.minjeok4go.petplace.common.constant.RefType;
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
    private RefType refType;

    @NotNull
    private String src;

    @NotNull
    private Integer sort;

}
