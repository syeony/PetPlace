// File: src/main/java/com/minjeok4go/petplace/hotel/dto/ReservationResponse.java
package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.AvailableDate;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "예약 정보 응답 DTO")
@Getter @Builder
public class ReservationResponse {

    @Schema(description = "예약 ID", example = "101")
    private Long id;
    @Schema(description = "예약한 사용자 ID", example = "5")
    private Long userId;
    @Schema(description = "예약된 반려동물 ID", example = "10")
    private Long petId;
    @Schema(description = "예약된 호텔 ID", example = "1")
    private Long hotelId;
    @Schema(description = "호텔 이름", example = "댕댕이 월드 호텔")
    private String hotelName;
    @Schema(description = "예약된 날짜 전체 목록", example = "[\"2024-08-15\", \"2024-08-16\"]")
    private List<LocalDate> reservedDates;
    @Schema(description = "체크인 날짜", example = "2024-08-15")
    private LocalDate checkInDate;
    @Schema(description = "체크아웃 날짜", example = "2024-08-17")
    private LocalDate checkOutDate;
    @Schema(description = "총 숙박일수", example = "2")
    private int totalDays;
    @Schema(description = "총 결제 금액", example = "300000.00")
    private BigDecimal totalPrice;
    @Schema(description = "예약 상태", example = "CONFIRMED")
    private Reservation.ReservationStatus status;
    @Schema(description = "특별 요청 사항", example = "창가 쪽 방으로 부탁드려요.")
    private String specialRequests;
    @Schema(description = "예약 생성 시각", example = "2024-08-01T10:00:00")
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
     * Reservation 엔티티와 Hotel 엔티티로부터 DTO 생성 (호텔 정보 직접 전달)
     */
    public static ReservationResponse from(Reservation reservation, com.minjeok4go.petplace.hotel.entity.Hotel hotel) {
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
                .hotelName(hotel.getName()) // 직접 전달받은 hotel 정보 사용
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