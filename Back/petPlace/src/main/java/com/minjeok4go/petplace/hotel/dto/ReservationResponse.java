package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long petId;
    private Long hotelId;
    private String hotelName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private BigDecimal totalPrice;
    private Reservation.ReservationStatus status;
    private String specialRequests;
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .petId(reservation.getPetId())
                .hotelId(reservation.getHotelId())
                .hotelName(reservation.getHotel() != null ? reservation.getHotel().getName() : null)
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .specialRequests(reservation.getSpecialRequests())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
