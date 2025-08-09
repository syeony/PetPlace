package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Schema(description = "호텔 검색 요청 DTO")
@Getter @Setter
public class HotelSearchRequest {

    @Schema(description = "반려동물 타입", example = "DOG", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "반려동물 종류는 필수입니다.")
    private Hotel.PetType petType;

    @Schema(description = "체크인 날짜", example = "2024-08-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @Schema(description = "체크아웃 날짜", example = "2024-08-17", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    @Schema(description = "최저 가격 (선택)", example = "100000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal minPrice;

    @Schema(description = "최고 가격 (선택)", example = "300000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal maxPrice;

    @Schema(description = "지역 필터 (주소 일부)", example = "서울", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String region;

    // 선택적 필터 옵션들
    private java.math.BigDecimal latitude; // 위치 기반 검색용
    private java.math.BigDecimal longitude;
    private Double radiusKm; // 반경 (km)
}