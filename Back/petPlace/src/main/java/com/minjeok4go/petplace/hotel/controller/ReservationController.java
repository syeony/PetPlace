// File: src/main/java/com/minjeok4go/petplace/hotel/controller/ReservationController.java
package com.minjeok4go.petplace.hotel.controller;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.hotel.dto.ReservationCreateRequest;
import com.minjeok4go.petplace.hotel.dto.ReservationResponse;
import com.minjeok4go.petplace.hotel.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservation", description = "예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * ⭐ 수정된 예약 생성 API - 날짜 선택 방식
     */
    @Operation(summary = "예약 생성", description = "선택한 날짜들로 호텔 예약을 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReservationCreateRequest request) {

        Long userId = Long.valueOf(userDetails.getUsername()); // JWT에서 사용자 ID 추출

        log.info("예약 생성 요청: userId={}, hotelId={}, selectedDates={}, petId={}",
                userId, request.getHotelId(), request.getSelectedDates(), request.getPetId());

        // 연속된 날짜인지 검증 (필요시)
        if (!request.isConsecutiveDates()) {
            log.warn("비연속 날짜 예약 시도: userId={}, selectedDates={}", userId, request.getSelectedDates());
            // 비연속 날짜 예약을 허용하지 않는 경우 예외 처리
            // throw new IllegalArgumentException("연속된 날짜만 예약할 수 있습니다.");
        }

        ReservationResponse reservation = reservationService.createReservation(userId, request);

        return ResponseEntity.ok(ApiResponse.success("예약 생성 성공", reservation));
    }

    /**
     * 사용자의 예약 목록 조회
     */
    @Operation(summary = "내 예약 목록 조회", description = "현재 사용자의 모든 예약을 조회합니다")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.valueOf(userDetails.getUsername());

        log.info("사용자 예약 목록 조회: userId={}", userId);

        List<ReservationResponse> reservations = reservationService.getUserReservations(userId);

        return ResponseEntity.ok(ApiResponse.success("예약 목록 조회 성공", reservations));
    }

    /**
     * 예약 상세 조회
     */
    @Operation(summary = "예약 상세 조회", description = "특정 예약의 상세 정보를 조회합니다")
    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reservationId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        log.info("예약 상세 조회: userId={}, reservationId={}", userId, reservationId);

        ReservationResponse reservation = reservationService.getReservation(userId, reservationId);

        return ResponseEntity.ok(ApiResponse.success("예약 상세 조회 성공", reservation));
    }

    /**
     * 예약 취소
     */
    @Operation(summary = "예약 취소", description = "예약을 취소하고 예약된 날짜들을 다시 예약 가능하게 만듭니다")
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<String>> cancelReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reservationId) {

        Long userId = Long.valueOf(userDetails.getUsername());

        log.info("예약 취소 요청: userId={}, reservationId={}", userId, reservationId);

        reservationService.cancelReservation(userId, reservationId);

        return ResponseEntity.ok(ApiResponse.success("예약 취소 성공", "예약이 성공적으로 취소되었습니다."));
    }

    /**
     * 예약 확정 (결제 완료 후 호출)
     */
    @Operation(summary = "예약 확정", description = "결제 완료 후 예약을 확정 상태로 변경합니다")
    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<ApiResponse<String>> confirmReservation(@PathVariable Long reservationId) {

        log.info("예약 확정: reservationId={}", reservationId);

        reservationService.confirmReservation(reservationId);

        return ResponseEntity.ok(ApiResponse.success("예약 확정 성공", "예약이 성공적으로 확정되었습니다."));
    }

    /**
     * 예약 완료 처리 (체크아웃 후)
     */
    @Operation(summary = "예약 완료 처리", description = "체크아웃 후 예약을 완료 상태로 변경합니다")
    @PutMapping("/{reservationId}/complete")
    public ResponseEntity<ApiResponse<String>> completeReservation(@PathVariable Long reservationId) {

        log.info("예약 완료 처리: reservationId={}", reservationId);

        reservationService.completeReservation(reservationId);

        return ResponseEntity.ok(ApiResponse.success("예약 완료 처리 성공", "예약이 성공적으로 완료되었습니다."));
    }

    /**
     * 예약 가능 여부 확인 (미리 체크용)
     */
    @Operation(summary = "예약 가능 여부 확인", description = "실제 예약 전에 선택한 날짜들이 예약 가능한지 확인합니다")
    @PostMapping("/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @RequestParam Long hotelId,
            @RequestBody List<java.time.LocalDate> selectedDates) {

        log.info("예약 가능 여부 확인: hotelId={}, selectedDates={}", hotelId, selectedDates);

        // AvailableDateService를 통해 확인하는 로직 추가 필요
        // 현재는 간단히 true 반환
        boolean isAvailable = true; // 실제로는 availableDateService.areAllDatesAvailable(hotelId, selectedDates);

        return ResponseEntity.ok(ApiResponse.success("예약 가능 여부 확인 완료", isAvailable));
    }
}