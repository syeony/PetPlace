package com.minjeok4go.petplace.auth.dto;

import com.minjeok4go.petplace.common.constant.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 로그인 요청 DTO")
public class SocialLoginRequest {

    @Schema(description = "소셜 플랫폼 종류", example = "KAKAO", requiredMode = Schema.RequiredMode.REQUIRED)
    private SocialProvider provider;

    @Schema(description = "소셜 플랫폼에서 발급받은 Access Token", example = "z2_kC..._j4", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;
}

