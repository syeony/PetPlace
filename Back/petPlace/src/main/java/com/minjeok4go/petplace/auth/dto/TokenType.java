package com.minjeok4go.petplace.auth.dto;

/**
 * JWT 토큰 타입을 정의하는 Enum
 * 하드코딩된 문자열 대신 타입 안전성을 제공
 */
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * 문자열로부터 TokenType을 찾는 메서드
     * @param value 토큰 타입 문자열
     * @return 매칭되는 TokenType 또는 null
     */
    public static TokenType fromString(String value) {
        for (TokenType type : TokenType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 문자열 값 반환
     * @return 토큰 타입 문자열
     */
    public String getValue() {
        return this.value;
    }
}