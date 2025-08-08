package com.minjeok4go.petplace.hotel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ReservationCreateRequest {

    @NotNull(message = "반려동물 ID는 필수입니다.")
    private Long petId;

    @NotNull(message = "호텔 ID는 필수입니다.")
    private Long hotelId;

    @NotNull(message = "체크인 날짜는 필수입니다.")
    private LocalDateTime checkIn;

    @NotNull(message = "체크아웃 날짜는 필수입니다.")
    private LocalDateTime checkOut;

    private String specialRequests;
}
