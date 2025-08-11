package com.minjeok4go.petplace.profile.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateIntroductionRequest {

    @NotNull
    private String content;
}
