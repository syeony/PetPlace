// 파일: common/dto/ErrorResponse.java
package com.minjeok4go.petplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private String code;
    private LocalDateTime timestamp;
    private String path;

    public static ErrorResponse of(String message, String code, String path) {
        return new ErrorResponse(false, message, code, LocalDateTime.now(), path);
    }

    public static ErrorResponse of(String message, String path) {
        return new ErrorResponse(false, message, "INTERNAL_ERROR", LocalDateTime.now(), path);
    }
}