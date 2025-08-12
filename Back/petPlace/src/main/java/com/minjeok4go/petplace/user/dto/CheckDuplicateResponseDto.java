package com.minjeok4go.petplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "아이디/닉네임 중복 체크 응답 DTO")
public class CheckDuplicateResponseDto {

    @Schema(description = "중복 여부 (true: 중복됨, false: 사용 가능)", example = "false")
    private Boolean duplicate;

    @Schema(description = "응답 메시지", example = "사용 가능한 아이디입니다.")
    private String message;
}
