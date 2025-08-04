
package com.minjeok4go.petplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequestDto {

    @NotBlank(message = "아이디는 필수입니다")
    private String userName;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    private String name;

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    private Long regionId;
    private String ci;
    private String phoneNumber;
    private String gender;
    private LocalDate birthday;

    // 본인인증 정보 (필수적으로 필요함 )
    @NotBlank(message = "본인인증을 완료해주세요")
    private String impUid; // 포트원에서 받은 거래번호 ->  이것을 통해서 연동 함
}