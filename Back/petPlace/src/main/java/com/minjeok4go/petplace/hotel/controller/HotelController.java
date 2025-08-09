// File: src/main/java/com/minjeok4go/petplace/hotel/controller/HotelController.java
package com.minjeok4go.petplace.hotel.controller;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.hotel.dto.HotelResponse;
import com.minjeok4go.petplace.hotel.dto.HotelSearchRequest;
import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hotel", description = "호텔 관련 API")
public class HotelController {

    private final HotelService hotelService;

    /**
     * ⭐ 수정된 호텔 검색 API - 날짜 기반 시스템
     */
    @Operation(summary = "호텔 검색", description = "반려동물 타입과 날짜 범위로 예약 가능한 호텔을 검색합니다")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotels(
            @Valid @RequestBody HotelSearchRequest request) {

        log.info("호텔 검색 요청: petType={}, startDate={}, endDate={}",
                request.getPetType(), request.getStartDate(), request.getEndDate());

        List<HotelResponse> hotels = hotelService.findAvailableHotels(request);

        return ResponseEntity.ok(ApiResponse.success("호텔 검색 성공", hotels));
    }

    /**
     * 호텔 상세 정보 조회
     */
    @Operation(summary = "호텔 상세 조회", description = "특정 호텔의 상세 정보를 조회합니다")
    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotelDetail(@PathVariable Long hotelId) {
        log.info("호텔 상세 조회: hotelId={}", hotelId);

        HotelResponse hotel = hotelService.getHotelDetail(hotelId);

        return ResponseEntity.ok(ApiResponse.success("호텔 상세 조회 성공", hotel));
    }

    /**
     * 특정 호텔의 예약 가능한 날짜 조회
     */
    @Operation(summary = "예약 가능 날짜 조회", description = "특정 호텔의 예약 가능한 날짜들을 조회합니다")
    @GetMapping("/{hotelId}/available-dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("예약 가능 날짜 조회: hotelId={}, startDate={}, endDate={}", hotelId, startDate, endDate);

        List<LocalDate> availableDates = hotelService.getAvailableDates(hotelId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("예약 가능 날짜 조회 성공", availableDates));
    }

    /**
     * 반려동물 타입별 호텔 조회
     */
    @Operation(summary = "반려동물 타입별 호텔 조회", description = "특정 반려동물 타입을 지원하는 호텔들을 조회합니다")
    @GetMapping("/by-pet-type/{petType}")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getHotelsByPetType(
            @PathVariable Hotel.PetType petType) {

        log.info("반려동물 타입별 호텔 조회: petType={}", petType);

        List<HotelResponse> hotels = hotelService.findHotelsByPetType(petType);

        return ResponseEntity.ok(ApiResponse.success("반려동물 타입별 호텔 조회 성공", hotels));
    }

    /**
     * 호텔명으로 검색
     */
    @Operation(summary = "호텔명 검색", description = "호텔명으로 호텔을 검색합니다")
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotelsByName(
            @RequestParam String name) {

        log.info("호텔명 검색: name={}", name);

        List<HotelResponse> hotels = hotelService.searchHotelsByName(name);

        return ResponseEntity.ok(ApiResponse.success("호텔명 검색 성공", hotels));
    }

    /**
     * 지역별 호텔 검색
     */
    @Operation(summary = "지역별 호텔 검색", description = "주소로 호텔을 검색합니다")
    @GetMapping("/search/address")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotelsByAddress(
            @RequestParam String address) {

        log.info("지역별 호텔 검색: address={}", address);

        List<HotelResponse> hotels = hotelService.searchHotelsByAddress(address);

        return ResponseEntity.ok(ApiResponse.success("지역별 호텔 검색 성공", hotels));
    }

    /**
     * 가격 범위별 호텔 검색
     */
    @Operation(summary = "가격 범위별 호텔 검색", description = "가격 범위와 반려동물 타입으로 호텔을 검색합니다")
    @GetMapping("/search/price-range")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotelsByPriceRange(
            @RequestParam java.math.BigDecimal minPrice,
            @RequestParam java.math.BigDecimal maxPrice,
            @RequestParam Hotel.PetType petType) {

        log.info("가격 범위별 호텔 검색: minPrice={}, maxPrice={}, petType={}",
                minPrice, maxPrice, petType);

        List<HotelResponse> hotels = hotelService.findHotelsByPriceRange(minPrice, maxPrice, petType);

        return ResponseEntity.ok(ApiResponse.success("가격 범위별 호텔 검색 성공", hotels));
    }

    /**
     * 모든 호텔 조회 (관리자용)
     */
    @Operation(summary = "모든 호텔 조회", description = "모든 호텔 목록을 조회합니다 (관리자용)")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getAllHotels() {
        log.info("모든 호텔 조회");

        List<HotelResponse> hotels = hotelService.getAllHotels();

        return ResponseEntity.ok(ApiResponse.success("모든 호텔 조회 성공", hotels));
    }

    /**
     * 호텔에 예약 가능 날짜 생성 (관리자용)
     */
    @Operation(summary = "예약 가능 날짜 생성", description = "특정 호텔에 예약 가능한 날짜들을 생성합니다 (관리자용)")
    @PostMapping("/{hotelId}/available-dates")
    public ResponseEntity<ApiResponse<String>> createAvailableDates(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("예약 가능 날짜 생성: hotelId={}, startDate={}, endDate={}", hotelId, startDate, endDate);

        hotelService.createAvailableDatesForHotel(hotelId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success("예약 가능 날짜 생성 완료",
                String.format("%s부터 %s까지의 예약 가능 날짜가 생성되었습니다.", startDate, endDate)));
    }
}