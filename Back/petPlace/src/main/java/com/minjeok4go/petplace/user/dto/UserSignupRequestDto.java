package com.minjeok4go.petplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "사용자 회원가입 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequestDto {

    @Schema(
        description = "사용자 아이디",
        example = "petlover123",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 4,
        maxLength = 20,
        pattern = "^[a-zA-Z0-9]+$"
    )
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용 가능합니다")
    private String userName;

    @Schema(
        description = "비밀번호",
        example = "petplace123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8,
        maxLength = 30,
        pattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"
    )
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하로 입력해주세요")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    @Schema(
        description = "사용자 닉네임",
        example = "멍멍이아빠",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 10
    )
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요")
    private String nickname;

    @Schema(
        description = "지역 ID",
        example = "1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        implementation = Long.class
    )
    private Long regionId;

    @Schema(
        description = "포트원 본인인증 거래번호 (imp_uid)",
        example = "imp_123456789",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "본인인증을 완료해주세요")
    private String impUid; // 포트원에서 받은 거래번호 ->  이것을 통해서 연동 함
}
