package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class HotelSearchRequest {

    @NotNull(message = "반려동물 종류는 필수입니다.")
    private Hotel.PetType petType;

    @NotNull(message = "체크인 날짜는 필수입니다.")
    private LocalDateTime checkIn;

    @NotNull(message = "체크아웃 날짜는 필수입니다.")
    private LocalDateTime checkOut;
}
