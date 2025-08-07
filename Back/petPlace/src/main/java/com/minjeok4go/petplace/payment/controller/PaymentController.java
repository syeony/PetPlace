package com.minjeok4go.petplace.payment.controller;

import com.minjeok4go.petplace.payment.dto.PaymentPrepareRequest;
import com.minjeok4go.petplace.payment.dto.PaymentPrepareResponse;
import com.minjeok4go.petplace.payment.dto.PaymentVerificationRequest;
import com.minjeok4go.petplace.payment.dto.PaymentResponse;
import com.minjeok4go.petplace.payment.entity.Payment;
import com.minjeok4go.petplace.payment.service.PaymentService;
import com.minjeok4go.petplace.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @PostMapping("/prepare")
    @Operation(summary = "결제 준비", description = "결제를 위한 주문번호와 금액을 생성합니다.")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @Valid @RequestBody PaymentPrepareRequest request) {

        Payment payment = paymentService.preparePayment(request.getReservationId());

        PaymentPrepareResponse response = PaymentPrepareResponse.builder()
                .merchantUid(payment.getMerchantUid())
                .amount(payment.getAmount())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "결제 검증", description = "포트원 결제를 검증하고 완료 처리합니다.")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {

        Payment payment = paymentService.verifyAndCompletePayment(request);

        // 결제 성공 시 푸시 알림 전송
        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            notificationService.sendPaymentSuccessNotification(payment);
        }

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    @GetMapping("/{merchantUid}")
    @Operation(summary = "결제 정보 조회", description = "주문번호로 결제 정보를 조회합니다.")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String merchantUid) {
        Payment payment = paymentService.findByMerchantUid(merchantUid);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}