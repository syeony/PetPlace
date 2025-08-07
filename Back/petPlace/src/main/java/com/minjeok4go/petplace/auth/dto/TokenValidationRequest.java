package com.minjeok4go.petplace.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenValidationRequest {
    private String token;
    
    public TokenValidationRequest(String token) {
        this.token = token;
    }
}
