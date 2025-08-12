package com.minjeok4go.petplace.missing.entity;

import com.minjeok4go.petplace.user.entity.User;
import com.minjeok4go.petplace.pet.entity.Pet;
import com.minjeok4go.petplace.region.entity.Region;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "missing_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MissingStatus status = MissingStatus.MISSING;

    @Column(name = "missing_at", nullable = false)
    private LocalDateTime missingAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public MissingReport(User user, Pet pet, Region region, String address,
                         BigDecimal latitude, BigDecimal longitude, String content,
                         LocalDateTime missingAt) {
        this.user = user;
        this.pet = pet;
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.content = content;
        this.missingAt = missingAt;
    }

    public void updateStatus(MissingStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateInfo(Pet pet, Region region, String address, BigDecimal latitude, BigDecimal longitude, String content, LocalDateTime missingAt) {
        this.pet = pet;
        this.region = region;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.content = content;
        this.missingAt = missingAt;
    }

    public enum MissingStatus {
        MISSING, FOUND, CANCELLED
    }
}