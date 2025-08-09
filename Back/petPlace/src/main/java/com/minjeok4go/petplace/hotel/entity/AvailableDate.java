package com.minjeok4go.petplace.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "available_dates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hotel_date", columnNames = {"hotel_id", "date"})
        },
        indexes = {
                @Index(name = "idx_hotel_date_status", columnList = "hotel_id, date, status"),
                @Index(name = "idx_date_status", columnList = "date, status")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AvailableDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 예약 가능 상태를 나타내는 enum
     */
    public enum AvailabilityStatus {
        AVAILABLE,  // 예약 가능
        BOOKED      // 예약 완료
    }

    /**
     * 예약 가능 여부 확인
     */
    public boolean isAvailable() {
        return this.status == AvailabilityStatus.AVAILABLE;
    }

    /**
     * 예약 상태로 변경
     */
    public void markAsBooked() {
        this.status = AvailabilityStatus.BOOKED;
    }

    /**
     * 예약 가능 상태로 변경 (예약 취소 시 사용)
     */
    public void markAsAvailable() {
        this.status = AvailabilityStatus.AVAILABLE;
    }
}