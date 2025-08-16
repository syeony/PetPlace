package com.minjeok4go.petplace.notification.repository;

import com.minjeok4go.petplace.common.constant.NotificationType;
import com.minjeok4go.petplace.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Slice<Notification> findByTargetUserIdAndTypeNotOrderByCreatedAtDesc(
        Long targetUserId,
        NotificationType type,
        Pageable pageable
    );
}
