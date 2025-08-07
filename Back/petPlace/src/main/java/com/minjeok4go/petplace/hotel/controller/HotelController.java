package com.minjeok4go.petplace.hotel.controller;

import com.minjeok4go.petplace.hotel.dto.HotelSearchRequest;
import com.minjeok4go.petplace.hotel.dto.HotelResponse;
import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotel", description = "호텔 관련 API")
public class HotelController {

    private final HotelService hotelService;

    @PostMapping("/search")
    @Operation(summary = "호텔 검색", description = "조건에 맞는 예약 가능한 호텔 목록을 조회합니다.")
    public ResponseEntity<List<HotelResponse>> searchHotels(@Valid @RequestBody HotelSearchRequest request) {

        List<Hotel> hotels = hotelService.findAvailableHotels(
                request.getPetType(),
                request.getCheckIn(),
                request.getCheckOut()
        );

        List<HotelResponse> response = hotels.stream()
                .map(HotelResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "호텔 상세 조회", description = "특정 호텔의 상세 정보를 조회합니다.")
    public ResponseEntity<HotelResponse> getHotelDetail(@PathVariable Long hotelId) {
        Hotel hotel = hotelService.findById(hotelId);
        return ResponseEntity.ok(HotelResponse.from(hotel));
    }

    @GetMapping
    @Operation(summary = "전체 호텔 목록 조회", description = "모든 호텔 목록을 조회합니다.")
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        List<Hotel> hotels = hotelService.findAllHotels();
        List<HotelResponse> response = hotels.stream()
                .map(HotelResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}