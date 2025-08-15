package com.minjeok4go.petplace.notification.controller;

import com.minjeok4go.petplace.notification.dto.NotificationResponse;
import com.minjeok4go.petplace.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
        summary = "내 알림 보관함",
        description = "토큰으로 받아온 유저 ID가 받은 알림들을\n" +
                "createdAt을 기준으로 내림차순 정렬하여 slice해 반환합니다"
    )
    @GetMapping("/me")
    public Slice<NotificationResponse> getMyNotifications(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @AuthenticationPrincipal String tokenUserId
    ) {
        return notificationService.getMyNotifications(tokenUserId, page, size);
    }
}
