package com.minjeok4go.petplace.care.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CareImageRequest {

    @NotNull
    private String src;

    @NotNull
    private Integer sort;
}