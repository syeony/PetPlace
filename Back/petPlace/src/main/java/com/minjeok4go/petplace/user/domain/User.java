package com.minjeok4go.petplace.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users") // 데이터베이스의 'User' 테이블과 매핑됩니다.
@EntityListeners(AuditingEntityListener.class) // ✅ JPA Auditing 추가
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요합니다.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Integer id;

    @Column(name = "user_name", nullable = false, unique = true, length = 20)
    private String userName;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @CreatedDate // ✅ 생성 시간 자동 저장
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // ✅ 수정 시간 자동 저장
    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    @Column(name = "region_id", nullable = false)
    private Long regionId; // ⭐️ 나중에 Region 엔티티와 @ManyToOne 관계로 변경될 수 있습니다.

    @Column(name = "default_pet_id")
    private Integer defaultPetId; // ⭐️ 나중에 Pet 엔티티와 @OneToOne 관계로 변경될 수 있습니다.

    @Column(length = 200)
    private String kakaoOauth;

    @Column(length = 500)
    private String userImgSrc;

    @Column(nullable = false)
    private BigDecimal petSmell;

    @Column(name = "default_badge_id")
    private Integer defaultBadgeId; // ⭐️ 나중에 Badge 엔티티와 관계로 변경될 수 있습니다.

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
    public User(String userName, String password, String name, String nickname, Long regionId, String ci, String phoneNumber, String gender, LocalDate birthday) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.regionId = regionId;
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