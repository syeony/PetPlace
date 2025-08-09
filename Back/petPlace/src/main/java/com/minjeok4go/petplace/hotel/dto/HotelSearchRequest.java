package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class HotelSearchRequest {

    @NotNull(message = "반려동물 종류는 필수입니다.")
    private Hotel.PetType petType;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    // 선택적 필터 옵션들
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;
    private String region; // 지역 필터
    private java.math.BigDecimal latitude; // 위치 기반 검색용
    private java.math.BigDecimal longitude;
    private Double radiusKm; // 반경 (km)
}