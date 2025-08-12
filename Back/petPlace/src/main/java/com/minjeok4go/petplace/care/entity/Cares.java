package com.minjeok4go.petplace.care.entity;

import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.region.entity.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cares")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cares {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareCategory category;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @Column(nullable = false)
    private Integer views = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareStatus status = CareStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Cares(String title, String content, User user, Pet pet, Region region,
                 CareCategory category, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.pet = pet;
        this.region = region;
        this.category = category;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    public void updateInfo(String title, String content, Pet pet, Region region,
                           CareCategory category, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.title = title;
        this.content = content;
        this.pet = pet;
        this.region = region;
        this.category = category;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    public void updateStatus(CareStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseViews() {
        this.views++;
    }

    public enum CareCategory {
        WALK_WANT("산책 구인"),     // 산책시켜줄 사람 구함
        WALK_REQ("산책견 구인"),    // 산책시킬 강아지 구함
        CARE_WANT("돌봄 구인"),     // 돌봐줄 사람 구함
        CARE_REQ("돌봄견 구인");    // 돌볼 반려동물 구함

        private final String description;

        CareCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum CareStatus {
        ACTIVE,     // 활성
        MATCHED,    // 매칭완료
        COMPLETED,  // 완료
        CANCELLED   // 취소
    }
}
