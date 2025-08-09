package com.minjeok4go.petplace.payment.controller;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.notification.service.NotificationService;
import com.minjeok4go.petplace.payment.dto.PaymentPrepareRequest;
import com.minjeok4go.petplace.payment.dto.PaymentPrepareResponse;
import com.minjeok4go.petplace.payment.dto.PaymentResponse;
import com.minjeok4go.petplace.payment.dto.PaymentVerificationRequest;
import com.minjeok4go.petplace.payment.entity.Payment;
import com.minjeok4go.petplace.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "💳 Payment", description = "결제 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Operation(
            summary = "결제 정보 사전 등록 (결제 준비)",
            description = """
            프론트엔드에서 포트원 결제창을 호출하기 전에, 서버에 결제될 정보를 미리 등록하고 고유 주문번호를 생성합니다.
            
            ### 프로세스
            1. 사용자가 예약을 생성하면 `reservationId`가 발급됩니다.
            2. 결제하기 버튼을 누르면, 이 API를 `reservationId`와 함께 호출합니다.
            3. 서버는 해당 예약 정보를 바탕으로 결제 금액을 확정하고, 고유 주문번호(`merchantUid`)를 생성하여 응답합니다.
            4. 프론트엔드는 이 `merchantUid`와 `amount`를 사용하여 포트원 결제창을 띄웁니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 준비 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\": false, \"message\": \"예약 정보를 찾을 수 없습니다.\", \"data\": null}")))
    })
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "결제 준비 요청 정보", required = true, content = @Content(schema = @Schema(implementation = PaymentPrepareRequest.class)))
            @RequestBody PaymentPrepareRequest request) {
        Payment payment = paymentService.preparePayment(request.getReservationId());
        return ResponseEntity.ok(ApiResponse.success("결제 준비 성공", PaymentPrepareResponse.from(payment)));
    }

    @Operation(
            summary = "결제 사후 검증 및 완료 처리",
            description = """
            사용자가 포트원 결제창에서 결제를 완료한 후, 서버에서 해당 결제가 위변조되지 않았는지 검증하고 최종적으로 결제 완료 처리합니다.
            
            ### 프로세스
            1. 프론트엔드에서 포트원 결제가 성공하면 `imp_uid`와 `merchant_uid`를 받습니다.
            2. 이 API를 `imp_uid`와 `merchant_uid`와 함께 호출합니다.
            3. 서버는 포트원 API에 직접 결제 정보를 조회하여, `/prepare` 단계에서 등록된 금액과 실제 결제된 금액이 일치하는지 검증합니다.
            4. 검증이 성공하면 예약 상태를 'CONFIRMED'로 변경하고, 결제 데이터를 최종 저장합니다.
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 검증 및 완료 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "결제 검증 실패 (금액 불일치 등)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\": false, \"message\": \"결제 검증에 실패했습니다: 결제 금액이 일치하지 않습니다.\", \"data\": null}")))
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "결제 검증 요청 정보", required = true, content = @Content(schema = @Schema(implementation = PaymentVerificationRequest.class)))
            @RequestBody PaymentVerificationRequest request) {
        Payment payment = paymentService.verifyAndCompletePayment(request);
        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            notificationService.sendPaymentSuccessNotification(payment);
        }
        return ResponseEntity.ok(ApiResponse.success("결제 검증 및 완료 성공", PaymentResponse.from(payment)));
    }

    @Operation(summary = "결제 정보 조회", description = "주문번호(`merchantUid`)를 사용하여 특정 결제 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    @GetMapping("/{merchantUid}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "조회할 결제의 주문번호", required = true, example = "petplace_1723123456789")
            @PathVariable String merchantUid) {
        Payment payment = paymentService.findByMerchantUid(merchantUid);
        return ResponseEntity.ok(ApiResponse.success("결제 정보 조회 성공", PaymentResponse.from(payment)));
    }
}
