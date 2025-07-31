package com.minjeok4go.petplace.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestDto {
    private String userId;
    private String password;

    public UserLoginRequestDto(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}
