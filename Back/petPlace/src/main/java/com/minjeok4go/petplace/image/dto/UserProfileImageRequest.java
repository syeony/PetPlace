package com.minjeok4go.petplace.image.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileImageRequest {

    @NotNull
    private String imgSrc;
}
