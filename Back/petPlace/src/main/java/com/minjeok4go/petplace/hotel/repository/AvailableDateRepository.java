package com.minjeok4go.petplace.hotel.repository;

import com.minjeok4go.petplace.hotel.entity.AvailableDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailableDateRepository extends JpaRepository<AvailableDate, Long> {

    /**
     * 특정 호텔의 날짜 범위와 상태로 예약 가능 날짜 조회
     */
    @Query("SELECT a FROM AvailableDate a WHERE a.hotelId = :hotelId " +
            "AND a.date BETWEEN :startDate AND :endDate " +
            "AND a.status = :status " +
            "ORDER BY a.date")
    List<AvailableDate> findByHotelIdAndDateBetweenAndStatus(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AvailableDate.AvailabilityStatus status
    );

    /**
     * 특정 호텔의 특정 날짜들이 모두 예약 가능한지 확인
     */
    @Query("SELECT COUNT(a) FROM AvailableDate a WHERE a.hotelId = :hotelId " +
            "AND a.date IN :dates AND a.status = 'AVAILABLE'")
    long countAvailableDatesByHotelIdAndDates(
            @Param("hotelId") Long hotelId,
            @Param("dates") List<LocalDate> dates
    );

    /**
     * 특정 날짜들의 ID로 상태를 BOOKED로 일괄 업데이트
     */
    @Modifying
    @Query("UPDATE AvailableDate a SET a.status = 'BOOKED' WHERE a.id IN :ids")
    void updateStatusToBookedByIds(@Param("ids") List<Long> ids);

    /**
     * 특정 날짜들의 ID로 상태를 AVAILABLE로 일괄 업데이트 (예약 취소 시 사용)
     */
    @Modifying
    @Query("UPDATE AvailableDate a SET a.status = 'AVAILABLE' WHERE a.id IN :ids")
    void updateStatusToAvailableByIds(@Param("ids") List<Long> ids);

    /**
     * 특정 호텔의 특정 날짜들 조회 (상태 무관)
     */
    @Query("SELECT a FROM AvailableDate a WHERE a.hotelId = :hotelId " +
            "AND a.date IN :dates ORDER BY a.date")
    List<AvailableDate> findByHotelIdAndDates(
            @Param("hotelId") Long hotelId,
            @Param("dates") List<LocalDate> dates
    );

    /**
     * 특정 호텔의 예약 가능한 날짜 개수 조회 (날짜 범위 내)
     */
    @Query("SELECT COUNT(a) FROM AvailableDate a WHERE a.hotelId = :hotelId " +
            "AND a.date BETWEEN :startDate AND :endDate AND a.status = 'AVAILABLE'")
    long countAvailableDates(
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 호텔의 기존 날짜 데이터 존재 여부 확인
     */
    boolean existsByHotelIdAndDate(Long hotelId, LocalDate date);

    /**
     * 특정 호텔의 모든 예약 가능 날짜 조회
     */
    List<AvailableDate> findByHotelIdAndStatusOrderByDate(
            Long hotelId,
            AvailableDate.AvailabilityStatus status
    );
}