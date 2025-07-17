package com.ssafy.api.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailSendRequest {
    @Email
    @NotBlank
    private String email;
}
