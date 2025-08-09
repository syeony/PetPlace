package com.minjeok4go.petplace.hotel.repository;

import com.minjeok4go.petplace.hotel.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 사용자 ID로 예약 목록 조회 (최신순)
     */
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 호텔 ID로 예약 목록 조회
     */
    List<Reservation> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    /**
     * 사용자 ID와 예약 상태로 예약 목록 조회
     */
    List<Reservation> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            Reservation.ReservationStatus status
    );

    /**
     * 특정 기간의 예약 목록 조회 (관리자용)
     */
    @Query("SELECT r FROM Reservation r JOIN r.reservedDates rd " +
            "WHERE rd.date BETWEEN :startDate AND :endDate " +
            "ORDER BY r.createdAt DESC")
    List<Reservation> findReservationsByDateRange(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );

    /**
     * 사용자의 특정 호텔 예약 이력 조회
     */
    List<Reservation> findByUserIdAndHotelIdOrderByCreatedAtDesc(Long userId, Long hotelId);

    /**
     * 예약 상태별 개수 조회
     */
    long countByStatus(Reservation.ReservationStatus status);

    /**
     * 사용자의 활성 예약 개수 조회 (PENDING, CONFIRMED)
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId " +
            "AND r.status IN ('PENDING', 'CONFIRMED')")
    long countActiveReservationsByUserId(@Param("userId") Long userId);

    /**
     * 다가오는 예약 조회 (알림 스케줄러용)
     */
    @Query("SELECT r FROM Reservation r JOIN r.reservedDates rd " +
            "WHERE r.status = 'CONFIRMED' " +
            "AND rd.date BETWEEN CAST(:start AS date) AND CAST(:end AS date) " +
            "ORDER BY rd.date ASC")
    List<Reservation> findUpcomingReservations(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}