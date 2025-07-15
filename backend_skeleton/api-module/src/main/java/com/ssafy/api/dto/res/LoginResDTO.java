package com.ssafy.api.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResDTO {
    private Long id;
    private String token;     // (JWT 등)
    private String nickname;
    private int output;
    private String msg;
}