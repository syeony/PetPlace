package com.ssafy.batch.dto.notification;

import com.google.gson.JsonObject;
import lombok.Data;

/**
 * 푸시 알림 DTO
 */
@Data
public class NotificationData {

    private String title = "";
    private String body = "";

    @Override
    public String toString() {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", this.title);
        jsonObject.addProperty("body", this.body);

        return jsonObject.toString();
    }
}

