package com.minjeok4go.petplace.like.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateLikeRequest {

    @NotNull
    private Long feedId;
}
