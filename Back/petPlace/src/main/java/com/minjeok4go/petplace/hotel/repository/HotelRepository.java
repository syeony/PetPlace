package com.minjeok4go.petplace.hotel.repository;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h FROM Hotel h " +
            "WHERE :petType MEMBER OF h.supportedPetTypes " +
            "AND h.id NOT IN (" +
            "    SELECT r.hotelId FROM Reservation r " +
            "    WHERE r.status = 'CONFIRMED' " +
            "    AND NOT (r.checkOut <= :checkIn OR r.checkIn >= :checkOut)" +
            ")")
    List<Hotel> findAvailableHotels(@Param("petType") Hotel.PetType petType,
                                    @Param("checkIn") LocalDateTime checkIn,
                                    @Param("checkOut") LocalDateTime checkOut);
}