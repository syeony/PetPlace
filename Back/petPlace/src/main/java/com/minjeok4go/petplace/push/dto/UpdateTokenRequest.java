package com.minjeok4go.petplace.push.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTokenRequest {

    @NotBlank
    private String oldToken;

    @NotBlank
    private String newToken;

    private String appVersion;
}
