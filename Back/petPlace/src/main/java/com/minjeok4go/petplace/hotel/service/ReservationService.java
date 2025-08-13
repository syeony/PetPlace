package com.minjeok4go.petplace.hotel.service;

import com.minjeok4go.petplace.hotel.dto.ReservationCreateRequest;
import com.minjeok4go.petplace.hotel.dto.ReservationResponse;
import com.minjeok4go.petplace.hotel.entity.AvailableDate;
import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.hotel.exception.HotelNotFoundException;
import com.minjeok4go.petplace.hotel.exception.ReservationNotFoundException;
import com.minjeok4go.petplace.hotel.repository.HotelRepository;
import com.minjeok4go.petplace.hotel.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final HotelRepository hotelRepository;
    private final AvailableDateService availableDateService;

    /**
     *  날짜 기반 시스템
     */
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationCreateRequest request) {
        log.info("사용자 ID {}의 예약 생성 시작: 호텔 ID {}, 선택된 날짜들: {}",
                userId, request.getHotelId(), request.getSelectedDates());

        // 1. 호텔 존재 여부 확인
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException("호텔을 찾을 수 없습니다: " + request.getHotelId()));

        // 2. 선택된 날짜들이 모두 예약 가능한지 확인
        if (!availableDateService.areAllDatesAvailable(request.getHotelId(), request.getSelectedDates())) {
            throw new IllegalStateException("선택한 날짜 중 일부가 이미 예약되었거나 예약할 수 없습니다.");
        }

        // 3. 선택된 날짜들을 예약 완료 상태로 변경
        List<AvailableDate> bookedDates = availableDateService.bookDates(
                request.getHotelId(), request.getSelectedDates());

        // 4. 총 가격 계산 (1박당 가격 × 선택된 날짜 수)
        BigDecimal totalPrice = hotel.getPricePerNight().multiply(
                BigDecimal.valueOf(request.getSelectedDates().size()));

        // 5. 예약 엔티티 생성
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .petId(request.getPetId())
                .hotelId(request.getHotelId())
                .reservedDates(bookedDates)
                .totalPrice(totalPrice)
                .status(Reservation.ReservationStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();

        // 6. 예약 저장
        Reservation savedReservation = reservationRepository.save(reservation);

        log.info("예약 생성 완료: 예약 ID {}, 총 가격: {}", savedReservation.getId(), totalPrice);

        // 응답 생성 시 호텔 정보를 직접 전달
        return ReservationResponse.from(savedReservation, hotel);
    }

    /**
     * 예약 취소 (결제 취소 시 호출)
     */
    @Transactional
    public void cancelReservationByPayment(Long reservationId) {
        log.info("예약 취소: 예약 ID {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        // 예약된 날짜들을 다시 예약 가능 상태로 변경
        List<Long> availableDateIds = reservation.getReservedDates().stream()
                .map(AvailableDate::getId)
                .collect(Collectors.toList());

        availableDateService.releaseDates(availableDateIds);

        // 예약 상태를 취소로 변경
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("예약 취소 완료: 예약 ID {}", reservationId);
    }

    /**
     * 예약 취소 (사용자가 직접 취소)
     */
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        log.info("사용자 ID {}의 예약 취소 시작: 예약 ID {}", userId, reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        // 권한 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("예약을 취소할 권한이 없습니다.");
        }

        // 이미 취소된 예약인지 확인
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        // 예약된 날짜들을 다시 예약 가능 상태로 변경
        List<Long> availableDateIds = reservation.getReservedDates().stream()
                .map(AvailableDate::getId)
                .collect(Collectors.toList());

        availableDateService.releaseDates(availableDateIds);

        // 예약 상태를 취소로 변경
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("예약 취소 완료: 예약 ID {}", reservationId);
    }

    /**
     * 예약 확정 (결제 완료 후 호출)
     */
    @Transactional
    public void confirmReservation(Long reservationId) {
        log.info("예약 확정: 예약 ID {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }

    /**
     * 사용자의 예약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 예약 상세 조회
     */
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        // 권한 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("예약을 조회할 권한이 없습니다.");
        }

        return ReservationResponse.from(reservation);
    }

    /**
     * 예약 완료 처리 (체크아웃 후)
     */
    @Transactional
    public void completeReservation(Long reservationId) {
        log.info("예약 완료 처리: 예약 ID {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }

    /**
     * ID로 예약 조회 (PaymentService에서 사용)
     */
    @Transactional(readOnly = true)
    public Reservation findById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("예약을 찾을 수 없습니다: " + reservationId));
    }

    /**
     * 다가오는 예약 조회 (알림 스케줄러에서 사용)
     */
    @Transactional(readOnly = true)
    public List<Reservation> findUpcomingReservations(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findUpcomingReservations(start, end);
    }
}