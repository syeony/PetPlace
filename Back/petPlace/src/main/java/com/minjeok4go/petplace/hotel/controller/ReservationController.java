// src/main/java/com/minjeok4go/petplace/hotel/controller/ReservationController.java
package com.minjeok4go.petplace.hotel.controller;

import com.minjeok4go.petplace.hotel.dto.ReservationCreateRequest;
import com.minjeok4go.petplace.hotel.dto.ReservationResponse;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.hotel.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "예약 생성", description = "새로운 호텔 예약을 생성합니다.")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            Authentication authentication) {

        // JWT에서 사용자 ID 추출 (기존 인증 로직 활용)
        Long userId = getUserIdFromAuthentication(authentication);

        Reservation reservation = reservationService.createReservation(
                userId,
                request.getPetId(),
                request.getHotelId(),
                request.getCheckIn(),
                request.getCheckOut(),
                request.getSpecialRequests()
        );

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @GetMapping("/my")
    @Operation(summary = "내 예약 목록 조회", description = "현재 사용자의 예약 목록을 조회합니다.")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);

        List<Reservation> reservations = reservationService.findByUserId(userId);
        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "예약 상세 조회", description = "특정 예약의 상세 정보를 조회합니다.")
    public ResponseEntity<ReservationResponse> getReservationDetail(@PathVariable Long reservationId) {
        Reservation reservation = reservationService.findById(reservationId);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    // JWT에서 사용자 ID 추출하는 헬퍼 메서드 (기존 인증 로직에 맞게 수정 필요)
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // 기존 프로젝트의 JWT 구현에 맞게 수정해야 합니다.
        // 예시: return ((UserPrincipal) authentication.getPrincipal()).getId();
        return 1L; // 임시값
    }
}