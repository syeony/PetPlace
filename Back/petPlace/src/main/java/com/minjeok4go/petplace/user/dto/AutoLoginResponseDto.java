package com.minjeok4go.petplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoLoginResponseDto {
    private String userId;
    private String nickname;
    private String message;
    private boolean success;

    // 성공 응답 생성 메서드
    public static AutoLoginResponseDto success(String userId, String nickname) {
        return AutoLoginResponseDto.builder()
                .userId(userId)
                .nickname(nickname)
                .message("자동 로그인 성공")
                .success(true)
                .build();
    }

    // 실패 응답 생성 메서드
    public static AutoLoginResponseDto failure(String message) {
        return AutoLoginResponseDto.builder()
                .message(message)
                .success(false)
                .build();
    }
}

