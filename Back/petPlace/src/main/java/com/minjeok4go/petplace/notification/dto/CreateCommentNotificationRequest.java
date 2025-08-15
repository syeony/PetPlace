package com.minjeok4go.petplace.notification.dto;

import com.minjeok4go.petplace.common.constant.RefType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentNotificationRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private String senderNickname;

    @NotNull
    private RefType refType;

    @NotNull
    private Long refId;

    @NotNull
    private Long commentId;

    @NotNull
    private String preview;
}
