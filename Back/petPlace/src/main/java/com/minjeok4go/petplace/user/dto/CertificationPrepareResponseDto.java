package com.minjeok4go.petplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CertificationPrepareResponseDto {
    private String certificationUrl;
    private String merchantUid;
    private String message;

    public static CertificationPrepareResponseDto success(String certificationUrl, String merchantUid) {
        return new CertificationPrepareResponseDto(
                certificationUrl,
                merchantUid,
                "본인인증 URL이 생성되었습니다."
        );
    }
}
