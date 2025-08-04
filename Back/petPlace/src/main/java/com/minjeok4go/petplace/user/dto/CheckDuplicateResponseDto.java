package com.minjeok4go.petplace.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckDuplicateResponseDto {
    private Boolean duplicate;  // true: 중복됨, false: 사용 가능
    private String message;       // 응답 메시지
}
