package com.minjeok4go.petplace.push.entity;

import com.minjeok4go.petplace.push.dto.CreateTokenRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_device_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_token", columnNames = {"user_id", "token"})
        },
        indexes = {
                @Index(name = "idx_udt_user_active", columnList = "user_id, active"),
                @Index(name = "idx_udt_updated_at", columnList = "updated_at")
        }
)
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 256)
    private String token;

    @Column(name = "app_version", length = 32)
    private String appVersion;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserDeviceToken(Long userId, CreateTokenRequest req) {
        this.userId = userId;
        this.token = req.getToken();
        this.appVersion = req.getAppVersion();
        this.active = true;
    }

    public void refresh(CreateTokenRequest req) {
        this.token = req.getToken();
        this.appVersion = req.getAppVersion();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    public void deactivate() { this.active = false; }
}
