package com.minjeok4go.petplace.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
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
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true, length = 20)
    private String userName;

    // [개선] 소셜 로그인을 위해 nullable = true로 변경 (nullable=false 삭제)
    @Column(length = 200)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @CreatedDate // ✅ 생성 시간 자동 저장
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime deletedAt;

    @Column(name = "region_id") // nullable = true로 변경 (테스트용)
    private Long regionId; // ⭐️ 나중에 Region 엔티티와 @ManyToOne 관계로 변경될 수 있습니다.

    @Column(name = "default_pet_id")
    private Integer defaultPetId; // ⭐️ 나중에 Pet 엔티티와 @OneToOne 관계로 변경될 수 있습니다.

    // [개선] 컬럼명 일반화 (kakaoOauth -> socialId) 및 unique 속성 추가
    @Column(name = "social_id", length = 200, unique = true)
    private String socialId;

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
    // [개선] 로그인 타입(EMAIL, KAKAO 등)을 구분하는 컬럼 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", length = 20, nullable = false)
    private LoginType loginType;

    // [개선] 소셜 계정의 이메일을 저장하는 컬럼 추가
    @Column(name = "social_email", length = 100)
    private String socialEmail;

    // [개선] 기존 이메일 회원가입용 빌더 수정
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
        this.loginType = LoginType.EMAIL; // 이메일 가입자는 EMAIL 타입으로 설정
        // 기본값 설정
        this.petSmell = new BigDecimal("36.5");
        this.isForeigner = false;
        this.level = 1;
        this.experience = 0;
    }

    // [개선] 신규 소셜 회원가입용 팩토리 메서드 추가
    public static User createSocialUser(String name, String nickname, Long regionId, String ci,
                                        String phoneNumber, String gender, LocalDate birthday,
                                        String socialId, String socialEmail, String profileImageUrl, LoginType loginType) {
        User user = new User();
        user.userName = loginType.name().toLowerCase() + "_" + socialId; // e.g., "kakao_12345"
        user.password = null; // 소셜 가입자는 비밀번호를 null로 설정
        user.name = name;
        user.nickname = nickname;
        user.regionId = regionId;
        user.ci = ci;
        user.phoneNumber = phoneNumber;
        user.gender = gender;
        user.birthday = birthday;
        user.loginType = loginType;
        user.socialId = socialId;
        user.socialEmail = socialEmail;
        user.userImgSrc = profileImageUrl;
        // 기본값 설정
        user.petSmell = new BigDecimal("36.5");
        user.isForeigner = false;
        user.level = 1;
        user.experience = 0;
        return user;
    }

    // [개선] 기존 이메일 계정 -> 소셜 계정 연동 메서드 추가
    public void linkSocialAccount(String socialId, String socialEmail, String profileImageUrl) {
        if (this.socialId != null) {
            throw new IllegalStateException("이미 다른 소셜 계정과 연동된 계정입니다.");
        }
        this.socialId = socialId;
        this.socialEmail = socialEmail;
        // 소셜 프로필 이미지가 있다면 업데이트
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.userImgSrc = profileImageUrl;
        }
    }
}