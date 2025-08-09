package com.minjeok4go.petplace.hotel.controller;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.hotel.dto.HotelResponse;
import com.minjeok4go.petplace.hotel.dto.HotelSearchRequest;
import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "🏨 Hotel & Search", description = "호텔 정보 조회 및 검색 관련 API")
public class HotelController {

    private final HotelService hotelService;

    @Operation(
            summary = "호텔 통합 검색",
            description = "체크인/체크아웃 날짜와 반려동물 타입 기준으로 예약 가능한 호텔 목록을 검색합니다. 가격, 지역 등 추가 필터링을 지원합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "호텔 검색 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 검색 요청 (필수 값 누락 등)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"반려동물 종류는 필수입니다.\", \"data\": null}"))
            )
    })
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotels(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "호텔 검색 필터 조건",
                    required = true,
                    content = @Content(schema = @Schema(implementation = HotelSearchRequest.class))
            )
            @RequestBody HotelSearchRequest request) {
        List<HotelResponse> hotels = hotelService.findAvailableHotels(request);
        return ResponseEntity.ok(ApiResponse.success("호텔 검색 성공", hotels));
    }

    @Operation(summary = "호텔 상세 정보 조회", description = "특정 호텔의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "호텔 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "해당 호텔을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"해당 호텔을 찾을 수 없습니다.\", \"data\": null}"))
            )
    })
    @GetMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotelDetail(
            @Parameter(description = "조회할 호텔의 ID", required = true, example = "1") @PathVariable Long hotelId) {
        HotelResponse hotel = hotelService.getHotelDetail(hotelId);
        return ResponseEntity.ok(ApiResponse.success("호텔 상세 조회 성공", hotel));
    }

    @Operation(summary = "예약 가능 날짜 조회", description = "특정 호텔의 특정 기간 동안 예약 가능한 날짜 목록을 조회합니다.")
    @GetMapping("/{hotelId}/available-dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(
            @Parameter(description = "조회할 호텔의 ID", required = true, example = "1") @PathVariable Long hotelId,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD)", required = true, example = "2024-08-10") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD)", required = true, example = "2024-08-20") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LocalDate> availableDates = hotelService.getAvailableDates(hotelId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("예약 가능 날짜 조회 성공", availableDates));
    }

    @Operation(summary = "반려동물 타입별 호텔 조회", description = "특정 반려동물 타입을 지원하는 모든 호텔을 조회합니다.")
    @GetMapping("/by-pet-type/{petType}")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getHotelsByPetType(
            @Parameter(description = "조회할 반려동물 타입", required = true, example = "DOG") @PathVariable Hotel.PetType petType) {
        List<HotelResponse> hotels = hotelService.findHotelsByPetType(petType);
        return ResponseEntity.ok(ApiResponse.success("반려동물 타입별 호텔 조회 성공", hotels));
    }

    @Operation(summary = "호텔명으로 검색", description = "호텔 이름에 검색어가 포함된 호텔 목록을 조회합니다.")
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotelsByName(
            @Parameter(description = "검색할 호텔 이름", required = true, example = "월드") @RequestParam String name) {
        List<HotelResponse> hotels = hotelService.searchHotelsByName(name);
        return ResponseEntity.ok(ApiResponse.success("호텔명 검색 성공", hotels));
    }

    @Operation(summary = "지역(주소)으로 호텔 검색", description = "주소에 검색어가 포함된 호텔 목록을 조회합니다.")
    @GetMapping("/search/address")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotelsByAddress(
            @Parameter(description = "검색할 주소 키워드", required = true, example = "강남") @RequestParam String address) {
        List<HotelResponse> hotels = hotelService.searchHotelsByAddress(address);
        return ResponseEntity.ok(ApiResponse.success("지역별 호텔 검색 성공", hotels));
    }

    @Operation(summary = "가격 범위별 호텔 검색", description = "지정된 가격 범위와 반려동물 타입에 맞는 호텔 목록을 조회합니다.")
    @GetMapping("/search/price-range")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotelsByPriceRange(
            @Parameter(description = "최저 가격", required = true, example = "50000") @RequestParam BigDecimal minPrice,
            @Parameter(description = "최고 가격", required = true, example = "200000") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "반려동물 타입", required = true, example = "DOG") @RequestParam Hotel.PetType petType) {
        List<HotelResponse> hotels = hotelService.findHotelsByPriceRange(minPrice, maxPrice, petType);
        return ResponseEntity.ok(ApiResponse.success("가격 범위별 호텔 검색 성공", hotels));
    }

    @Operation(summary = "모든 호텔 조회 (관리자용)", description = "시스템에 등록된 모든 호텔 목록을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getAllHotels() {
        List<HotelResponse> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(ApiResponse.success("모든 호텔 조회 성공", hotels));
    }

    @Operation(summary = "예약 가능 날짜 생성 (관리자용)", description = "특정 호텔에 예약 가능한 날짜들을 일괄 생성합니다.")
    @PostMapping("/{hotelId}/available-dates")
    public ResponseEntity<ApiResponse<String>> createAvailableDates(
            @Parameter(description = "호텔 ID", required = true, example = "1") @PathVariable Long hotelId,
            @Parameter(description = "생성 시작 날짜 (YYYY-MM-DD)", required = true, example = "2024-09-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "생성 종료 날짜 (YYYY-MM-DD)", required = true, example = "2024-09-30") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        hotelService.createAvailableDatesForHotel(hotelId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("예약 가능 날짜 생성 완료", String.format("%s부터 %s까지의 예약 가능 날짜가 생성되었습니다.", startDate, endDate)));
    }
}
