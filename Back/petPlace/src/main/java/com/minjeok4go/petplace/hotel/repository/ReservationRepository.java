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

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findByStatusAndCheckInBetween(
            Reservation.ReservationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status = 'CONFIRMED' " +
            "AND r.checkIn BETWEEN :start AND :end")
    List<Reservation> findUpcomingReservations(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}