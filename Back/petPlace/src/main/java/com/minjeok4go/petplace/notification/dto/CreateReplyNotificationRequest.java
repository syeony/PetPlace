package com.minjeok4go.petplace.notification.dto;

import com.minjeok4go.petplace.common.constant.RefType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateReplyNotificationRequest extends CreateCommentNotificationRequest {
    public CreateReplyNotificationRequest(Long targetUserId, String senderNickname, RefType refType,
                                          Long refId, Long commentId, String preview){
        super(targetUserId, senderNickname, refType, refId, commentId, preview);
    }
}