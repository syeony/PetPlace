package com.minjeok4go.petplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오에서 받은 사용자 정보를 담는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfo {
    private String socialId;        // 카카오 고유 ID
    private String email;           // 카카오 계정 이메일
    private String nickname;        // 카카오 프로필 닉네임
    private String profileImage;    // 카카오 프로필 이미지 URL
}