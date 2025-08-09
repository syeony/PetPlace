package com.minjeok4go.petplace.hotel.dto;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

@Getter @Builder
public class HotelResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal pricePerNight;
    private BigDecimal totalPrice;  // 총 가격 필드 추가
    private Integer maxCapacity;
    private Set<Hotel.PetType> supportedPetTypes;
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