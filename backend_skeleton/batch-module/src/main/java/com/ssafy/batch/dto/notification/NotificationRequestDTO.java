package com.ssafy.batch.dto.notification;

import lombok.Data;
import org.springframework.stereotype.Service;

/**
 * 푸시 알림 최종 request DTO
 */
@Service
@Data
public class NotificationRequestDTO {

    private String registration_ids;
    private NotificationData notification;
    private NotificationData data;

}
