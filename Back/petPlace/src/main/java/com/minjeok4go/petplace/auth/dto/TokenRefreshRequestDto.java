package com.minjeok4go.petplace.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 갱신 요청 DTO")
public class TokenRefreshRequestDto {

    @Schema(description = "토큰 갱신에 사용할 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
