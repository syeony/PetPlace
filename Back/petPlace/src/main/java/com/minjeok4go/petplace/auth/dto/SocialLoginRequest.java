package com.minjeok4go.petplace.auth.dto;

import com.minjeok4go.petplace.common.constant.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 요청 DTO (보안 강화 버전)
 * 이제 사용자 정보 대신 액세스 토큰만 받습니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    private SocialProvider provider;    // KAKAO, NAVER, GOOGLE
    private String accessToken;         // 카카오에서 발급받은 액세스 토큰
}
