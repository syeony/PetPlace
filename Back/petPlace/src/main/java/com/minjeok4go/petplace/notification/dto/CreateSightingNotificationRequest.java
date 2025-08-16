package com.minjeok4go.petplace.notification.dto;

import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.missing.entity.MissingReport;
import com.minjeok4go.petplace.missing.entity.Sighting;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSightingNotificationRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private String senderNickname;

    @NotNull
    private RefType refType;

    @NotNull
    private Long refId;

    @NotNull
    private MissingReport miss;

    @NotNull
    private Sighting sight;
}
