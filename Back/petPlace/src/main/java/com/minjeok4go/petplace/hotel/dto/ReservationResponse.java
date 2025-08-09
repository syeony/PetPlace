// File: src/main/java/com/minjeok4go/petplace/hotel/dto/ReservationResponse.java
package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.AvailableDate;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Builder
public class ReservationResponse {

    private Long id;
    private Long userId;
    private Long petId;
    private Long hotelId;
    private String hotelName;

    // ⭐ checkIn, checkOut 필드 제거
    // ⭐ 예약된 날짜들 목록으로 변경
    private List<LocalDate> reservedDates;
    private LocalDate checkInDate;  // 예약된 날짜 중 가장 이른 날짜
    private LocalDate checkOutDate; // 예약된 날짜 중 가장 늦은 날짜
    private int totalDays;          // 총 숙박일수

    private BigDecimal totalPrice;
    private Reservation.ReservationStatus status;
    private String specialRequests;
    private LocalDateTime createdAt;

    /**
     * Reservation 엔티티로부터 DTO 생성
     */
    public static ReservationResponse from(Reservation reservation) {
        // 예약된 날짜들을 LocalDate 리스트로 변환
        List<LocalDate> reservedDates = reservation.getReservedDates().stream()
                .map(AvailableDate::getDate)
                .sorted() // 날짜순 정렬
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .petId(reservation.getPetId())
                .hotelId(reservation.getHotelId())
                .hotelName(reservation.getHotel() != null ? reservation.getHotel().getName() : null)
                .reservedDates(reservedDates)
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .totalDays(reservation.getTotalDays())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .specialRequests(reservation.getSpecialRequests())
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    /**
     * 연속된 날짜 예약인지 확인
     */
    public boolean isConsecutiveReservation() {
        if (reservedDates == null || reservedDates.size() <= 1) {
            return true;
        }

        for (int i = 1; i < reservedDates.size(); i++) {
            if (!reservedDates.get(i).equals(reservedDates.get(i-1).plusDays(1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 예약 기간 문자열 생성 (UI 표시용)
     */
    public String getReservationPeriod() {
        if (checkInDate != null && checkOutDate != null) {
            if (checkInDate.equals(checkOutDate)) {
                return checkInDate.toString() + " (1박)";
            } else {
                return checkInDate.toString() + " ~ " + checkOutDate.toString() + " (" + totalDays + "박)";
            }
        }
        return "날짜 정보 없음";
    }
}