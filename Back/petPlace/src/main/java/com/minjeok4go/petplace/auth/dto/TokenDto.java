package com.minjeok4go.petplace.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String userId;
    private String nickname;
    private String userImgSrc;
    private Integer level;
    private Long defaultPetId;
    private Long rid;
}
