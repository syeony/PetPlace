package com.ssafy.core.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// 가입 타입으로 사용될 코드값
public enum JoinCode implements BaseEnumCode<String> {

    none("none"),
    sns("sns"),
    NULL("");

    public static JoinCode fromValue(String v) {
        // 대소문자 구분 없이 받고 싶다면 toLowerCase() 등 추가
        return JoinCode.valueOf(v);
    }
    private final String value;
}

