package com.ssafy.api.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignUpReqDTO {

    @NotBlank
    @Schema(
            description = "uid (일반회원:아이디, sns로그인:uid값)",
            example = "kakao123",
            required = true
    )
    private String uid;

    @JsonProperty("type")
    private String type = "none";

    @JsonSetter("type")
    public void setType(String type) {
        if (type == null || type.isBlank()) {
            this.type = "none";
        } else {
            this.type = type;
        }
    }

    @NotBlank
    @Schema(description = "비밀번호", example = "123", required = true)
    private String password;

    @Schema(description = "이름", example = "카카오")
    private String name;

    @Schema(description = "이메일", example = "kakao123@test.com")
    private String email;

    @Schema(description = "핸드폰번호('-' 값 없이 입력)", example = "01012345678")
    private String phone;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "상세주소")
    private String addressDetail;
}
