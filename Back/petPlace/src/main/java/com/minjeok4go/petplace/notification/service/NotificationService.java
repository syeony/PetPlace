package com.minjeok4go.petplace.notification.service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.minjeok4go.petplace.auth.service.AuthService;
import com.minjeok4go.petplace.common.constant.NotificationType;
import com.minjeok4go.petplace.common.constant.RefType;
import com.minjeok4go.petplace.notification.dto.*;
import com.minjeok4go.petplace.notification.entity.Notification;
import com.minjeok4go.petplace.notification.repository.NotificationRepository;
import com.minjeok4go.petplace.push.entity.UserDeviceToken;
import com.minjeok4go.petplace.push.repository.UserDeviceTokenRepository;
import com.minjeok4go.petplace.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebase;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public Slice<NotificationResponse> getMyNotifications(String tokenUserId, int page, int size) {

        User me = authService.getUserFromToken(tokenUserId);

        // createdAt DESC 고정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<Notification> slice = notificationRepository
                .findByTargetUserIdAndTypeNotOrderByCreatedAtDesc(
                        me.getId(), NotificationType.CHAT, pageable
                );

        return slice.map(NotificationResponse::new);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateCommentNotificationRequest req) {
        String title = "새 댓글이 달렸어요";
        String body = req.getSenderNickname() + ": " + req.getPreview();

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.COMMENT,
                req.getRefType(),
                req.getRefId(),
                title,
                body,
                Map.of("refType",req.getRefType().name(),
                        "refId", String.valueOf(req.getRefId()),
                        "commentId", String.valueOf(req.getCommentId()))
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateReplyNotificationRequest req) {
        String title = "내 댓글에 새 답글이 달렸어요";
        String body = req.getSenderNickname() + ": " + req.getPreview();

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.COMMENT,
                req.getRefType(),
                req.getRefId(),
                title,
                body,
                Map.of("refType",req.getRefType().name(),
                        "refId", String.valueOf(req.getRefId()),
                        "commentId", String.valueOf(req.getCommentId()))
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateLikeNotificationRequest req) {
        String title = "내 글에 좋아요가 눌렸어요";
        String body  = req.getSenderNickname() + "님이 내 글을 좋아합니다";

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.LIKE,
                RefType.FEED,
                req.getRefId(),
                title,
                body,
                Map.of("refType", RefType.FEED.name(),
                        "refId", String.valueOf(req.getRefId()))
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateChatNotificationRequest req) {
        String title = "새 채팅 메세지가 왔어요";
        String body  = req.getSenderNickname() + ": " + req.getPreview();

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.CHAT,
                RefType.CHAT,
                req.getRoomId(),
                title,
                body,
                Map.of("refType", RefType.CHAT.name(),
                        "refId", String.valueOf(req.getRoomId()),
                        "chatId", String.valueOf(req.getChatId()))
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CreateSightingNotificationRequest req) {
        String title = "실종 신고가 매칭됐어요";
        String body  = req.getSenderNickname() + "님이 " + req.getMiss().getPet().getName() + "를 발견했습니다";

        sendAndStore(
                req.getTargetUserId(),
                NotificationType.SIGHT,
                RefType.SIGHTING,
                req.getRefId(),
                title,
                body,
                Map.of("refType", RefType.SIGHTING.name(),
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


        log.debug("[FCM] sent: {}", notification);

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
