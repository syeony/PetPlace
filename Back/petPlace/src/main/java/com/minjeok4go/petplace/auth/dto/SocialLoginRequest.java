package com.minjeok4go.petplace.auth.dto;

import com.minjeok4go.petplace.common.constant.SocialProvider;
import com.minjeok4go.petplace.user.dto.KakaoUserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    private SocialProvider provider;    // KAKAO, NAVER, GOOGLE
    private KakaoUserInfo userInfo;     // 소셜 플랫폼에서 받은 사용자 정보
}