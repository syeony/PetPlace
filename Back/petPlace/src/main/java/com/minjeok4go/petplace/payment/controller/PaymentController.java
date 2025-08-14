package com.minjeok4go.petplace.payment.controller;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.notification.service.FCMNotificationService;
import com.minjeok4go.petplace.payment.dto.PaymentPrepareRequest;
import com.minjeok4go.petplace.payment.dto.PaymentPrepareResponse;
import com.minjeok4go.petplace.payment.dto.PaymentResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "💳 Payment", description = "결제 관련 API")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final FCMNotificationService FCMNotificationService;

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
            @Valid @RequestBody PaymentPrepareRequest request) {
        Payment payment = paymentService.preparePayment(request.getReservationId());
        return ResponseEntity.ok(ApiResponse.success("결제 준비 성공", PaymentPrepareResponse.from(payment)));
    }

    @Operation(summary = "결제 정보 조회", description = "주문번호(`merchantUid`)를 사용하여 특정 결제 정보를 조회합니다. 프론트엔드는 이 API를 주기적으로 호출하여 최종 결제 상태를 확인합니다.")
    @GetMapping("/{merchantUid}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "조회할 결제의 주문번호", required = true, example = "petplace_1723123456789")
            @PathVariable String merchantUid) {
        Payment payment = paymentService.findByMerchantUid(merchantUid);
        return ResponseEntity.ok(ApiResponse.success("결제 정보 조회 성공", PaymentResponse.from(payment)));
    }

    @Operation(
            summary = "포트원 웹훅 수신 (서버 전용)",
            description = "포트원에서 결제 상태 변경 시 자동으로 호출되는 엔드포인트입니다. 결제 완료 처리를 전담합니다.",
            security = {} // 이 엔드포인트는 JWT 인증이 필요 없음을 명시
    )
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp) {

        log.info("포트원 웹훅 수신 - webhookId: {}", webhookId);

        try {
            paymentService.handleWebhook(webhookId, webhookSignature, webhookTimestamp, payload);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("웹훅 처리 실패 - webhookId: {}, error: {}", webhookId, e.getMessage(), e);
            // 에러가 발생해도 포트원은 200 OK를 받아야 재시도를 하지 않습니다.
            // 하지만 어떤 에러인지 서버에 기록하고 빠르게 조치하는 것이 중요합니다.
            return ResponseEntity.status(HttpStatus.OK).body("Webhook processed with error");
        }
    }

    @Operation(
            summary = "포트원 V1 웹훅 수신 (서버 전용)",
            description = "포트원 V1 형식의 웹훅을 처리합니다.",
            security = {} // 이 엔드포인트는 JWT 인증이 필요 없음을 명시
    )
    @PostMapping("/webhook/v1")
    public ResponseEntity<String> handleV1Webhook(@RequestBody Map<String, Object> payload) {
        
        String impUid = (String) payload.get("imp_uid");
        String merchantUid = (String) payload.get("merchant_uid");
        String status = (String) payload.get("status");
        
        log.info("포트원 V1 웹훅 수신 - imp_uid: {}, merchant_uid: {}, status: {}", impUid, merchantUid, status);

        try {
            if ("paid".equals(status)) {
                paymentService.processV1PaidPayment(impUid, merchantUid);
            } else if ("cancelled".equals(status)) {
                paymentService.processV1CancelledPayment(impUid, merchantUid);
            }
            return ResponseEntity.ok("V1 Webhook processed successfully");
        } catch (Exception e) {
            log.error("V1 웹훅 처리 실패 - imp_uid: {}, error: {}", impUid, e.getMessage(), e);
            return ResponseEntity.ok("V1 Webhook processed with error");
        }
    }

}
