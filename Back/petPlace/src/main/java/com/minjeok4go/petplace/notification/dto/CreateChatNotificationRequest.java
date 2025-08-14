package com.minjeok4go.petplace.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatNotificationRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private Long roomId;

    @NotNull
    private String preview;
}
