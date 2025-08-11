package com.minjeok4go.petplace.missing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MissingImageRequest {

    @NotNull
    private String src;

    @NotNull
    private Integer sort;
}