package com.minjeok4go.petplace.auth.dto;

import com.minjeok4go.petplace.common.constant.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 회원가입 요청 DTO")
public class SocialSignupRequest {

    @Schema(description = "소셜 플랫폼 종류", example = "KAKAO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "소셜 플랫폼 정보는 필수입니다.")
    private SocialProvider provider;

    @Schema(description = "소셜 로그인 시 발급받은 임시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJ...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "임시 토큰은 필수입니다.")
    private String tempToken;

    @Schema(description = "포트원 본인인증 고유 ID", example = "imp_123456789012", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본인인증 정보(imp_uid)는 필수입니다.")
    private String impUid;

    @Schema(description = "사용할 닉네임 (2~10자)", example = "새로운집사", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자로 입력해주세요.")
    private String nickname;

    @Schema(description = "활동 지역(동네) ID", example = "11", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "지역 ID는 필수입니다.")
    private Long regionId;

    @Schema(description = "연동할 기존 계정의 사용자 ID (연동 시에만 사용)", example = "123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long linkUserId;
}

