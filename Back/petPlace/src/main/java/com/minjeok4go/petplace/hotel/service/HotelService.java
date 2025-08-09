package com.minjeok4go.petplace.hotel.service;

import com.minjeok4go.petplace.hotel.dto.HotelResponse;
import com.minjeok4go.petplace.hotel.dto.HotelSearchRequest;
import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.exception.HotelNotFoundException;
import com.minjeok4go.petplace.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;
    private final AvailableDateService availableDateService;

    /**
     *  날짜 기반 시스템
     */
    public List<HotelResponse> findAvailableHotels(HotelSearchRequest request) {
        log.info("호텔 검색 시작: 반려동물 타입 {}, 시작일 {}, 종료일 {}",
                request.getPetType(), request.getStartDate(), request.getEndDate());

        // 날짜 유효성 검증
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜로는 예약할 수 없습니다.");
        }

        // 필요한 총 일수 계산 (시작일부터 종료일까지)
        long dayCount = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        if (dayCount <= 0) {
            throw new IllegalArgumentException("예약 기간이 유효하지 않습니다.");
        }

        // 새로운 검색 쿼리: 요청된 모든 날짜가 예약 가능한 호텔만 조회
        List<Hotel> availableHotels = hotelRepository.findAvailableHotels(
                request.getPetType(),
                request.getStartDate(),
                request.getEndDate(),
                dayCount
        );

        log.info("검색 결과: {}개 호텔 발견", availableHotels.size());

        return availableHotels.stream()
                .map(hotel -> HotelResponse.from(hotel, calculateTotalPrice(hotel, dayCount)))
                .collect(Collectors.toList());
    }

    /**
     * 호텔 상세 정보 조회
     */
    public HotelResponse getHotelDetail(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("호텔을 찾을 수 없습니다: " + hotelId));

        return HotelResponse.from(hotel, hotel.getPricePerNight());
    }

    /**
     * 특정 호텔의 예약 가능한 날짜 조회
     */
    public List<LocalDate> getAvailableDates(Long hotelId, LocalDate startDate, LocalDate endDate) {
        // 호텔 존재 여부 확인
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException("호텔을 찾을 수 없습니다: " + hotelId);
        }

        return availableDateService.getAvailableDates(hotelId, startDate, endDate)
                .stream()
                .map(availableDate -> availableDate.getDate())
                .collect(Collectors.toList());
    }

    /**
     * 모든 호텔 목록 조회 (관리자용)
     */
    public List<HotelResponse> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream()
                .map(hotel -> HotelResponse.from(hotel, hotel.getPricePerNight()))
                .collect(Collectors.toList());
    }

    /**
     * 반려동물 타입으로 호텔 검색
     */
    public List<HotelResponse> findHotelsByPetType(Hotel.PetType petType) {
        List<Hotel> hotels = hotelRepository.findByPetType(petType);

        return hotels.stream()
                .map(hotel -> HotelResponse.from(hotel, hotel.getPricePerNight()))
                .collect(Collectors.toList());
    }

    /**
     * 호텔명으로 검색
     */
    public List<HotelResponse> searchHotelsByName(String name) {
        List<Hotel> hotels = hotelRepository.findByNameContainingIgnoreCase(name);

        return hotels.stream()
                .map(hotel -> HotelResponse.from(hotel, hotel.getPricePerNight()))
                .collect(Collectors.toList());
    }

    /**
     * 지역으로 호텔 검색
     */
    public List<HotelResponse> searchHotelsByAddress(String address) {
        List<Hotel> hotels = hotelRepository.findByAddressContainingIgnoreCase(address);

        return hotels.stream()
                .map(hotel -> HotelResponse.from(hotel, hotel.getPricePerNight()))
                .collect(Collectors.toList());
    }

    /**
     * 가격 범위로 호텔 검색
     */
    public List<HotelResponse> findHotelsByPriceRange(
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            Hotel.PetType petType) {

        List<Hotel> hotels = hotelRepository.findByPriceRangeAndPetType(minPrice, maxPrice, petType);

        return hotels.stream()
                .map(hotel -> HotelResponse.from(hotel, hotel.getPricePerNight()))
                .collect(Collectors.toList());
    }

    /**
     * 총 가격 계산 (1박당 가격 × 숙박일수)
     */
    private java.math.BigDecimal calculateTotalPrice(Hotel hotel, long dayCount) {
        return hotel.getPricePerNight().multiply(java.math.BigDecimal.valueOf(dayCount));
    }

    /**
     * 호텔에 예약 가능 날짜 생성 (관리자용)
     */
    @Transactional
    public void createAvailableDatesForHotel(Long hotelId, LocalDate startDate, LocalDate endDate) {
        // 호텔 존재 여부 확인
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException("호텔을 찾을 수 없습니다: " + hotelId);
        }

        availableDateService.createAvailableDates(hotelId, startDate, endDate);
        log.info("호텔 ID {}에 대해 {}부터 {}까지 예약 가능 날짜 생성 완료", hotelId, startDate, endDate);
    }
}