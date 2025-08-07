package com.minjeok4go.petplace.hotel.service;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.hotel.repository.HotelRepository;
import com.minjeok4go.petplace.hotel.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final HotelRepository hotelRepository;

    public Reservation createReservation(Long userId, Long petId, Long hotelId,
                                         LocalDateTime checkIn, LocalDateTime checkOut,
                                         String specialRequests) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("호텔을 찾을 수 없습니다."));

        // 숙박 일수 계산
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 늦어야 합니다.");
        }

        // 총 금액 계산
        BigDecimal totalPrice = hotel.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .petId(petId)
                .hotelId(hotelId)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .totalPrice(totalPrice)
                .specialRequests(specialRequests)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = findById(reservationId);
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    public Reservation findById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByUserId(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findUpcomingReservations(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findUpcomingReservations(start, end);
    }
}