package com.minjeok4go.petplace.auth.dto;

import com.minjeok4go.petplace.common.constant.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 회원가입 요청 DTO (보안 강화 버전)
 * 이제 사용자 정보는 임시 토큰에서 추출합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialSignupRequest {
    private SocialProvider provider;        // 소셜 플랫폼
    private String tempToken;               // 소셜 로그인에서 받은 임시 토큰 (사용자 정보 포함)
    private String impUid;                  // 본인인증 거래번호
    private String nickname;                // 사용자가 설정할 닉네임
    private Long regionId;                  // 지역 ID
    private Long linkUserId;                // 연동할 기존 계정 ID (선택적)
}
