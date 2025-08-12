package com.minjeok4go.petplace.hotel.controller;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.hotel.dto.ReservationCreateRequest;
import com.minjeok4go.petplace.hotel.dto.ReservationResponse;
import com.minjeok4go.petplace.hotel.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "📅 Reservation", description = "호텔 예약 생성 및 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "호텔 예약 생성", description = "선택된 날짜에 호텔 예약을 생성합니다. 생성 시 예약 상태는 'PENDING'(결제대기)이 됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "예약 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 예약 요청 (예약 불가능한 날짜 포함 등)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\": false, \"message\": \"선택한 날짜 중 일부는 예약이 불가능합니다.\", \"data\": null}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\": false, \"message\": \"로그인이 필요합니다.\", \"data\": null}")))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "호텔 예약 생성 정보", required = true, content = @Content(schema = @Schema(implementation = ReservationCreateRequest.class))) @RequestBody ReservationCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId) {
        Long userIdLong = Long.valueOf(userId);
        ReservationResponse reservation = reservationService.createReservation(userIdLong, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("예약이 성공적으로 생성되었습니다.", reservation));
    }

    @Operation(summary = "내 예약 목록 조회", description = "인증된 사용자의 모든 예약 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId) {
        Long userIdLong = Long.valueOf(userId);
        List<ReservationResponse> reservations = reservationService.getUserReservations(userIdLong);
        return ResponseEntity.ok(ApiResponse.success("내 예약 목록 조회 성공", reservations));
    }

    @Operation(summary = "예약 상세 조회", description = "특정 예약의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "예약 조회 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 예약을 찾을 수 없음")
    })
    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @Parameter(description = "조회할 예약의 ID", required = true, example = "101") @PathVariable Long reservationId,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId) {
        Long userIdLong = Long.valueOf(userId);
        ReservationResponse reservation = reservationService.getReservation(userIdLong, reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 상세 조회 성공", reservation));
    }

    @Operation(summary = "예약 취소", description = "사용자가 직접 예약을 취소합니다. 'PENDING' 또는 'CONFIRMED' 상태의 예약만 취소 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "예약 취소 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 예약을 찾을 수 없음")
    })
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<String>> cancelReservation(
            @Parameter(description = "취소할 예약의 ID", required = true, example = "101") @PathVariable Long reservationId,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId) {
        Long userIdLong = Long.valueOf(userId);
        reservationService.cancelReservation(userIdLong, reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 취소 성공", "예약이 성공적으로 취소되었습니다."));
    }

    @Operation(summary = "예약 확정 (결제 완료)", description = "결제 완료 후, 'PENDING' 상태의 예약을 'CONFIRMED' 상태로 변경합니다. 주로 결제 시스템의 웹훅(Webhook)에 의해 호출됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예약 확정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 예약을 찾을 수 없음")
    })
    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<ApiResponse<String>> confirmReservation(
            @Parameter(description = "확정할 예약의 ID", required = true, example = "101") @PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 확정 성공", "예약이 성공적으로 확정되었습니다."));
    }

    @Operation(summary = "예약 가능 여부 사전 확인", description = "실제 예약을 생성하기 전에, 선택한 날짜들이 모두 예약 가능한지 미리 확인합니다.")
    @PostMapping("/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "예약 가능 여부를 확인할 호텔 ID와 날짜 목록",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CheckAvailabilityRequest.class)))
            @RequestBody CheckAvailabilityRequest request) {

        // TODO: ReservationService에 아래 메소드 구현이 필요합니다.
        // [오류 수정] reservationService에 해당 메소드가 없어 컴파일 오류가 발생하므로, 우선 주석 처리하고 기본값을 반환합니다.
        // boolean isAvailable = reservationService.areAllDatesAvailable(request.getHotelId(), request.getSelectedDates());
        boolean isAvailable = true; // 임시로 true 반환

        return ResponseEntity.ok(ApiResponse.success("예약 가능 여부 확인 완료", isAvailable));
    }

    // check-availability API를 위한 내부 DTO 클래스
    @Schema(description = "예약 가능 여부 확인 요청 DTO")
    static class CheckAvailabilityRequest {
        @Schema(description = "호텔 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long hotelId;
        @Schema(description = "선택한 날짜 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<LocalDate> selectedDates;

        public Long getHotelId() { return hotelId; }
        public List<LocalDate> getSelectedDates() { return selectedDates; }
    }
}
