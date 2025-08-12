// File: src/main/java/com/minjeok4go/petplace/hotel/entity/Reservation.java
package com.minjeok4go.petplace.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 기존 User 엔티티와 연결

    @Column(name = "pet_id", nullable = false)
    private Long petId; // 기존 Pet 엔티티와 연결

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "reservation_dates",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "available_date_id")
    )
    @Builder.Default
    private List<AvailableDate> reservedDates = new ArrayList<>();

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "special_requests", length = 1000)
    private String specialRequests;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ReservationStatus {
        PENDING,     // 결제 대기
        CONFIRMED,   // 예약 확정
        CANCELLED,   // 예약 취소
        COMPLETED    // 이용 완료
    }

    /**
     * 예약된 날짜 추가 편의 메소드
     */
    public void addReservedDate(AvailableDate availableDate) {
        this.reservedDates.add(availableDate);
    }

    /**
     * 예약된 날짜 제거 편의 메소드 (예약 취소 시 사용)
     */
    public void removeReservedDate(AvailableDate availableDate) {
        this.reservedDates.remove(availableDate);
    }

    /**
     * 예약된 총 일수 계산
     */
    public int getTotalDays() {
        return this.reservedDates.size();
    }

    /**
     * 체크인 날짜 (예약된 날짜 중 가장 이른 날짜)
     */
    public java.time.LocalDate getCheckInDate() {
        return this.reservedDates.stream()
                .map(AvailableDate::getDate)
                .min(java.time.LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * 체크아웃 날짜 (예약된 날짜 중 가장 늦은 날짜)
     */
    public java.time.LocalDate getCheckOutDate() {
        return this.reservedDates.stream()
                .map(AvailableDate::getDate)
                .max(java.time.LocalDate::compareTo)
                .orElse(null);
    }
}