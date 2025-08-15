package com.minjeok4go.petplace.notification.dto;

import com.minjeok4go.petplace.feed.entity.Feed;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLikeNotificationRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private String senderNickname;

    @NotNull
    private Long refId;

    @NotNull
    private Feed feed;

}
