package com.minjeok4go.petplace.push.dto;

import com.minjeok4go.petplace.push.entity.UserDeviceToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private Long id;

    private String token;

    private String appVersion;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TokenResponse(UserDeviceToken token) {
        this.id = token.getId();
        this.token = token.getToken();
        this.appVersion = token.getAppVersion();
        this.active = token.isActive();
        this.createdAt = token.getCreatedAt();
        this.updatedAt = token.getUpdatedAt();
    }
}
