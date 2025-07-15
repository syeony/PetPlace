package com.ssafy.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_auth")
@Getter @Setter @NoArgsConstructor
public class EmailAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 인증할 이메일 주소 */
    @Column(nullable = false, length = 255)
    private String email;

    /** 발급된 인증번호(authNum) */
    @Column(name = "auth_num", nullable = false, length = 10)
    private String authNum;

    /** 만료 시각 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 사용 여부 (인증 성공 처리 시 true로 업데이트) */
    @Column(nullable = false)
    private Boolean used = false;

    /** 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
