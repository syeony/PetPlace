// src/main/java/com/minjeok4go/petplace/user/domain/User.java
package com.minjeok4go.petplace.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "User") // 데이터베이스의 'User' 테이블과 매핑됩니다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요합니다.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, length = 20)
    private String userId;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    private String nickname;

    @CreationTimestamp // INSERT 시 현재 시간을 자동으로 저장합니다.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Long rid; // ⭐️ 나중에 Region 엔티티와 @ManyToOne 관계로 변경될 수 있습니다.

    @Column
    private Long defaultPetId; // ⭐️ 나중에 Pet 엔티티와 @OneToOne 관계로 변경될 수 있습니다.

    @Column(length = 200)
    private String kakaoOauth;

    @Column(length = 500)
    private String userImgSrc;

    @Column(nullable = false)
    private BigDecimal petSmell;

    @Column
    private Long defaultBadgeId; // ⭐️ 나중에 Badge 엔티티와 관계로 변경될 수 있습니다.

    @Column(nullable = false, unique = true, length = 88)
    private String ci;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column
    private Boolean isForeigner;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer experience;

    // 회원가입 시 필요한 정보를 받는 빌더
    @Builder
    public User(String userId, String password, String name, String nickname, Long rid, String ci, String phoneNumber, String gender, LocalDate birthday) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.rid = rid;
        this.ci = ci;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.birthday = birthday;
        // 기본값 설정
        this.petSmell = new BigDecimal("36.5");
        this.isForeigner = false;
        this.level = 1;
        this.experience = 0;
    }
}