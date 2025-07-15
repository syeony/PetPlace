package com.ssafy.api.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignUpResDTO {
    private Long id;
    private int output;
    private String msg;
}
