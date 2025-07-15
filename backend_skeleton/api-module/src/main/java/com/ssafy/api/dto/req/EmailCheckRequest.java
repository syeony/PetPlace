package com.ssafy.api.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCheckRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String authNum;


}
