package com.minjeok4go.petplace.notification.service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.minjeok4go.petplace.common.constant.NotificationType;
import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.notification.dto.CreateCommentNotificationRequest;
import com.minjeok4go.petplace.notification.dto.CreateLikeNotificationRequest;
import com.minjeok4go.petplace.notification.entity.Notification;
import com.minjeok4go.petplace.notification.repository.NotificationRepository;
import com.minjeok4go.petplace.push.entity.UserDeviceToken;
import com.minjeok4go.petplace.push.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebase;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final NotificationRepository notificationRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateCommentNotificationRequest req) {
        String title = "새 댓글이 달렸어요";
        String body = req.getPreview();

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.COMMENT,
                req.getRefType(),
                req.getRefId(),
                title,
                body,
                Map.of("refType",req.getRefType().getDisplayName(),
                        "refId", String.valueOf(req.getRefId()),
                        "commentId", String.valueOf(req.getCommentId()))
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateLikeNotificationRequest req) {
        String title = "내 글에 좋아요가 눌렸어요";
        String body  = "누군가 내 글을 좋아합니다";

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.LIKE,
                RefType.FEED,
                req.getRefId(),
                title,
                body,
                Map.of("refType","FEED",
                        "refId", String.valueOf(req.getRefId()))
        );
    }

    protected void sendAndStore(Long targetUserId,
                                NotificationType notificationType,
                                RefType refType,
                                Long refId,
                                String title,
                                String body,
                                Map<String,String> data) {
        // 1) 알림 DB 저장
        Notification notification = notificationRepository.save(new Notification(
                targetUserId, notificationType, refType, refId, title, body, Map.copyOf(data)
        ));

        // 2) 토큰 조회
        List<String> tokens = userDeviceTokenRepository.findAllByUserIdAndActiveTrue(targetUserId)
                .stream().map(UserDeviceToken::getToken).toList();
        if (tokens.isEmpty()) return;

        // 3) 멀티 전송
        List<Message> messages = tokens.stream().map(t ->
                Message.builder()
                        .setToken(t)
                        .setNotification(NotificationCompat(title, body))
                        .putAllData(data)
                        .putData("notificationId", String.valueOf(notification.getId()))
                        .build()
        ).toList();

        try {
            var res = firebase.sendEach(messages);
            var responses = res.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    // 죽은 토큰 비활성화
                    userDeviceTokenRepository.findByUserIdAndToken(targetUserId, tokens.get(i))
                            .ifPresent(UserDeviceToken::deactivate);
                }
            }
        } catch (FirebaseMessagingException ex) {
            // 로깅/재시도는 환경에 맞게
        }
    }

    private static com.google.firebase.messaging.Notification NotificationCompat(String title, String body) {
        return com.google.firebase.messaging.Notification
                .builder().setTitle(title).setBody(body).build();
    }
}
