package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

@Schema(description = "호텔 정보 응답 DTO")
@Getter @Builder
public class HotelResponse {
    @Schema(description = "호텔 ID", example = "1")
    private Long id;
    @Schema(description = "호텔 이름", example = "댕댕이 월드 호텔")
    private String name;
    @Schema(description = "호텔 설명", example = "반려견과 함께하는 최고의 휴양지")
    private String description;
    @Schema(description = "호텔 주소", example = "서울특별시 강남구 테헤란로 123")
    private String address;
    @Schema(description = "전화번호", example = "02-123-4567")
    private String phoneNumber;
    @Schema(description = "위도", example = "37.5045")
    private BigDecimal latitude;
    @Schema(description = "경도", example = "127.0489")
    private BigDecimal longitude;
    @Schema(description = "1박당 가격", example = "150000.00")
    private BigDecimal pricePerNight;
    @Schema(description = "검색된 기간의 총 가격", example = "300000.00")
    private BigDecimal totalPrice;
    @Schema(description = "최대 수용 인원", example = "2")
    private Integer maxCapacity;
    @Schema(description = "숙박 가능한 반려동물 타입 목록", example = "[\"DOG\", \"CAT\"]")
    private Set<Hotel.PetType> supportedPetTypes;
    @Schema(description = "호텔 대표 이미지 URL", example = "https://example.com/hotel_image.jpg")
    private String imageUrl;

    public static HotelResponse from(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .phoneNumber(hotel.getPhoneNumber())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .pricePerNight(hotel.getPricePerNight())
                .totalPrice(hotel.getPricePerNight())  // 기본값으로 1박 가격 설정
                .maxCapacity(hotel.getMaxCapacity())
                .supportedPetTypes(hotel.getSupportedPetTypes())
                .imageUrl(hotel.getImageUrl())
                .build();
    }

    public static HotelResponse from(Hotel hotel, BigDecimal totalPrice) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .phoneNumber(hotel.getPhoneNumber())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .pricePerNight(hotel.getPricePerNight())
                .totalPrice(totalPrice)
                .maxCapacity(hotel.getMaxCapacity())
                .supportedPetTypes(hotel.getSupportedPetTypes())
                .imageUrl(hotel.getImageUrl())
                .build();
    }
}