package com.minjeok4go.petplace.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포트원 본인인증 결과 데이터 (내부용)")
public class VerificationData {
    @Schema(description = "연계정보(CI)", example = "8xJc...")
    private String ci;
    @Schema(description = "인증된 사용자 이름", example = "홍길동")
    private String name;
    @Schema(description = "인증된 휴대폰 번호", example = "01012345678")
    private String phone;
    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthDate;
    @Schema(description = "성별 (male/female)", example = "male")
    private String gender;
    @Schema(description = "외국인 여부", example = "false")
    private Boolean isForeigner;
}
