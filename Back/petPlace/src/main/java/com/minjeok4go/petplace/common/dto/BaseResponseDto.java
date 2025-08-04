package com.minjeok4go.petplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseResponseDto {
    protected String message;
    protected boolean success;
}