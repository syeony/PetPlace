package com.minjeok4go.petplace.notification.entity;

import com.minjeok4go.petplace.common.constant.NotificationType;
import com.minjeok4go.petplace.common.constant.RefType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_noti_target_created", columnList = "target_user_id, created_at"),
                @Index(name = "idx_noti_type_target",   columnList = "type, target_user_id")
        }
)
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 24)
    private RefType refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 300)
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_json", columnDefinition = "json")
    private Map<String, Object> dataJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Notification(
            Long targetUserId,
            NotificationType type,
            RefType refType,
            Long refId,
            String title,
            String body,
            Map<String, Object> dataJson
    ) {
        this.targetUserId = targetUserId;
        this.type = type;
        this.refType = refType;
        this.refId = refId;
        this.title = title;
        this.body = body;
        this.dataJson = dataJson;
    }
}
