package com.minjeok4go.petplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {

    @Schema(description = "사용자 아이디", example = "petlover123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "아이디는 필수입니다")
    private String userName;

    @Schema(description = "비밀번호", example = "petplace123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}