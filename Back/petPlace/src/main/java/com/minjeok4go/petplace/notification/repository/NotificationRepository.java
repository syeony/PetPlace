package com.minjeok4go.petplace.notification.repository;

import com.minjeok4go.petplace.common.constant.NotificationType;
import com.minjeok4go.petplace.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByTargetUserIdOrderByIdDesc(Long targetUserId, Pageable pageable);

    Page<Notification> findByTargetUserIdAndTypeOrderByIdDesc(Long targetUserId, NotificationType type, Pageable pageable);
}
