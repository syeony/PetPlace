package com.minjeok4go.petplace.hotel.service;

import com.minjeok4go.petplace.hotel.entity.AvailableDate;
import com.minjeok4go.petplace.hotel.repository.AvailableDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AvailableDateService {

    private final AvailableDateRepository availableDateRepository;

    /**
     * 특정 호텔에 대해 날짜 범위의 예약 가능 날짜들을 생성
     * 기존에 존재하는 날짜는 스킵
     */
    public void createAvailableDates(Long hotelId, LocalDate startDate, LocalDate endDate) {
        log.info("호텔 ID {}에 대해 {}부터 {}까지 예약 가능 날짜 생성 시작", hotelId, startDate, endDate);

        List<AvailableDate> availableDates = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 이미 존재하는 날짜는 스킵
            if (!availableDateRepository.existsByHotelIdAndDate(hotelId, currentDate)) {
                AvailableDate availableDate = AvailableDate.builder()
                        .hotelId(hotelId)
                        .date(currentDate)
                        .status(AvailableDate.AvailabilityStatus.AVAILABLE)
                        .build();
                availableDates.add(availableDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        if (!availableDates.isEmpty()) {
            availableDateRepository.saveAll(availableDates);
            log.info("호텔 ID {}에 대해 {}개의 예약 가능 날짜 생성 완료", hotelId, availableDates.size());
        }
    }

    /**
     * 선택된 날짜들이 모두 예약 가능한지 확인
     */
    @Transactional(readOnly = true)
    public boolean areAllDatesAvailable(Long hotelId, List<LocalDate> dates) {
        long availableCount = availableDateRepository.countAvailableDatesByHotelIdAndDates(hotelId, dates);
        boolean allAvailable = availableCount == dates.size();

        log.debug("호텔 ID {}, 요청 날짜 수: {}, 예약 가능 날짜 수: {}, 모두 예약 가능: {}",
                hotelId, dates.size(), availableCount, allAvailable);

        return allAvailable;
    }

    /**
     * 특정 날짜들을 예약 완료 상태로 변경
     */
    public List<AvailableDate> bookDates(Long hotelId, List<LocalDate> dates) {
        log.info("호텔 ID {}의 날짜들을 예약 완료 상태로 변경: {}", hotelId, dates);

        // 해당 날짜들 조회
        List<AvailableDate> availableDates = availableDateRepository.findByHotelIdAndDates(hotelId, dates);

        // 예약 가능한 날짜들만 필터링
        List<AvailableDate> bookableDates = availableDates.stream()
                .filter(AvailableDate::isAvailable)
                .collect(Collectors.toList());

        if (bookableDates.size() != dates.size()) {
            throw new IllegalStateException("일부 날짜가 이미 예약되었거나 존재하지 않습니다.");
        }

        // 상태를 BOOKED로 변경
        List<Long> ids = bookableDates.stream()
                .map(AvailableDate::getId)
                .collect(Collectors.toList());

        availableDateRepository.updateStatusToBookedByIds(ids);

        // 변경된 엔티티들의 상태 업데이트
        bookableDates.forEach(AvailableDate::markAsBooked);

        log.info("{}개 날짜를 예약 완료 상태로 변경 완료", bookableDates.size());
        return bookableDates;
    }

    /**
     * 특정 날짜들을 예약 가능 상태로 변경 (예약 취소 시 사용)
     */
    public void releaseDates(List<Long> availableDateIds) {
        log.info("날짜 ID들을 예약 가능 상태로 변경: {}", availableDateIds);
        availableDateRepository.updateStatusToAvailableByIds(availableDateIds);
    }

    /**
     * 특정 호텔의 예약 가능한 날짜 조회
     */
    @Transactional(readOnly = true)
    public List<AvailableDate> getAvailableDates(Long hotelId, LocalDate startDate, LocalDate endDate) {
        return availableDateRepository.findByHotelIdAndDateBetweenAndStatus(
                hotelId, startDate, endDate, AvailableDate.AvailabilityStatus.AVAILABLE);
    }

    /**
     * 특정 호텔의 연속된 예약 가능 날짜 수 확인
     */
    @Transactional(readOnly = true)
    public long getConsecutiveAvailableDaysCount(Long hotelId, LocalDate startDate, LocalDate endDate) {
        return availableDateRepository.countAvailableDates(hotelId, startDate, endDate);
    }
}