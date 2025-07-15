package com.ssafy.api.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdResDTO {
    @Schema(description = "회원 아이디", example = "1", required = true)
    private long id;
}
