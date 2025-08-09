// File: src/main/java/com/minjeok4go/petplace/hotel/repository/HotelRepository.java
package com.minjeok4go.petplace.hotel.repository;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * 특정 날짜 범위에서 모든 날짜가 예약 가능한 호텔들을 검색
     * 주어진 반려동물 타입을 지원하고, 요청된 모든 날짜에 대해 AVAILABLE 상태인 호텔만 반환
     */
    @Query("SELECT DISTINCT h FROM Hotel h " +
            "WHERE :petType MEMBER OF h.supportedPetTypes " +
            "AND h.id IN (" +
            "    SELECT a.hotelId FROM AvailableDate a " +
            "    WHERE a.date BETWEEN :startDate AND :endDate " +
            "    AND a.status = 'AVAILABLE' " +
            "    GROUP BY a.hotelId " +
            "    HAVING COUNT(a.id) = :dayCount" +
            ")")
    List<Hotel> findAvailableHotels(
            @Param("petType") Hotel.PetType petType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dayCount") long dayCount
    );

    /**
     * 특정 지역 내 호텔 검색 (위도, 경도 범위 기반)
     */
    @Query("SELECT h FROM Hotel h WHERE h.latitude BETWEEN :minLat AND :maxLat " +
            "AND h.longitude BETWEEN :minLng AND :maxLng " +
            "AND :petType MEMBER OF h.supportedPetTypes")
    List<Hotel> findHotelsByLocationAndPetType(
            @Param("minLat") java.math.BigDecimal minLat,
            @Param("maxLat") java.math.BigDecimal maxLat,
            @Param("minLng") java.math.BigDecimal minLng,
            @Param("maxLng") java.math.BigDecimal maxLng,
            @Param("petType") Hotel.PetType petType
    );

    /**
     * 반려동물 타입으로 호텔 검색
     */
    @Query("SELECT h FROM Hotel h WHERE :petType MEMBER OF h.supportedPetTypes")
    List<Hotel> findByPetType(@Param("petType") Hotel.PetType petType);

    /**
     * 가격 범위로 호텔 검색
     */
    @Query("SELECT h FROM Hotel h WHERE h.pricePerNight BETWEEN :minPrice AND :maxPrice " +
            "AND :petType MEMBER OF h.supportedPetTypes")
    List<Hotel> findByPriceRangeAndPetType(
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("petType") Hotel.PetType petType
    );

    /**
     * 호텔명으로 검색 (부분 일치)
     */
    List<Hotel> findByNameContainingIgnoreCase(String name);

    /**
     * 주소로 검색 (부분 일치)
     */
    List<Hotel> findByAddressContainingIgnoreCase(String address);
}