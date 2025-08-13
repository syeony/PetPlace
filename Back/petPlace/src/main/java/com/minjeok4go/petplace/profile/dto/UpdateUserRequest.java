package com.minjeok4go.petplace.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String nickname;

    private String curPassword;

    private String newPassword;

    private String imgSrc;

    private Long regionId;
}
