package com.minjeok4go.petplace.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileRequest {

    @NotBlank
    private Long userId;

    public ProfileRequest(Long userId){
        this.userId = userId;
    }
}
