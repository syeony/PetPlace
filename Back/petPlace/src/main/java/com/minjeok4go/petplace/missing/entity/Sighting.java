package com.minjeok4go.petplace.missing.entity;

import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.region.entity.Region;
import com.minjeok4go.petplace.common.constant.Breed;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sightings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sighting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "address", nullable = false, length = 300)
    private String address;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // AI 모델이 예측한 품종 (추가됨)
    @Enumerated(EnumType.STRING)
    @Column(name = "breed", length = 50)
    private Breed breed;

    @Column(name = "sighted_at", nullable = false)
    private LocalDateTime sightedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Sighting(User user, Region region, String address,
                    BigDecimal latitude, BigDecimal longitude, String content,
                    LocalDateTime sightedAt) {
        this.user = user;
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.content = content;
        this.sightedAt = sightedAt;
    }

    // AI 예측 결과를 업데이트하는 메서드
    public void updatePredictedBreed(Breed predictedBreed) {
        this.breed = predictedBreed;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateInfo(Region region, String address, BigDecimal latitude, BigDecimal longitude, String content, LocalDateTime sightedAt) {
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.content = content;
        this.sightedAt = sightedAt;
    }
}