package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.AvailableDate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter @Builder
public class AvailableDateResponse {

    private Long id;
    private Long hotelId;
    private LocalDate date;
    private AvailableDate.AvailabilityStatus status;
    private boolean isAvailable;

    /**
     * AvailableDate 엔티티로부터 DTO 생성
     */
    public static AvailableDateResponse from(AvailableDate availableDate) {
        return AvailableDateResponse.builder()
                .id(availableDate.getId())
                .hotelId(availableDate.getHotelId())
                .date(availableDate.getDate())
                .status(availableDate.getStatus())
                .isAvailable(availableDate.isAvailable())
                .build();
    }
}