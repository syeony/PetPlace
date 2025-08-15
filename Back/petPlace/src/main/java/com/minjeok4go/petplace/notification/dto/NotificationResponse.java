package com.minjeok4go.petplace.notification.dto;

import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.notification.entity.Notification;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String body;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private RefType refType;

    @NotNull
    private Long refId;

    @NotNull
    private Map<String, Object> dataJson;

    public NotificationResponse(Notification notification){
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.createdAt = notification.getCreatedAt();
        this.refType = notification.getRefType();
        this.refId = notification.getRefId();
        this.dataJson = notification.getDataJson();
    }
}
