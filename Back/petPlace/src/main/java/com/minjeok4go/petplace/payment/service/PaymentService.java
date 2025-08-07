package com.minjeok4go.petplace.payment.service;

import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.hotel.service.ReservationService;
import com.minjeok4go.petplace.payment.dto.PaymentVerificationRequest;
import com.minjeok4go.petplace.payment.dto.PortOneTokenResponse;
import com.minjeok4go.petplace.payment.dto.PortOnePaymentResponse;
import com.minjeok4go.petplace.payment.entity.Payment;
import com.minjeok4go.petplace.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationService reservationService;

    @Qualifier("portOneWebClient")
    private final WebClient webClient;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.secret-key}")
    private String secretKey;

    @Value("${portone.store-id}")
    private String storeId;

    public Payment preparePayment(Long reservationId) {
        Reservation reservation = reservationService.findById(reservationId);

        String merchantUid = generateMerchantUid(reservationId);

        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .merchantUid(merchantUid)
                .amount(reservation.getTotalPrice())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    public Payment verifyAndCompletePayment(PaymentVerificationRequest request) {
        log.info("결제 검증 시작 - merchantUid: {}, impUid: {}",
                request.getMerchantUid(), request.getImpUid());

        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        try {
            // 2. 포트원에서 결제 정보 조회
            String accessToken = getPortOneAccessToken();
            PortOnePaymentResponse portOnePayment = getPaymentFromPortOne(request.getImpUid(), accessToken);

            // 3. 결제 검증
            if (!validatePayment(payment, portOnePayment)) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason("결제 금액 불일치 또는 결제 상태 오류");
                paymentRepository.save(payment);
                throw new RuntimeException("결제 검증 실패");
            }

            // 4. 결제 완료 처리
            payment.setImpUid(request.getImpUid());
            payment.setStatus(Payment.PaymentStatus.PAID);
            payment.setPaymentMethod(determinePaymentMethod(portOnePayment.getResponse().getPay_method()));
            payment.setPaidAt(LocalDateTime.now());

            // 5. 예약 확정
            reservationService.confirmReservation(payment.getReservationId());

            log.info("결제 검증 성공 - merchantUid: {}", request.getMerchantUid());
            return paymentRepository.save(payment);  // ← 이 줄을 추가!

        } catch (Exception e) {
            log.error("결제 검증 실패 - merchantUid: {}, error: {}",
                    request.getMerchantUid(), e.getMessage(), e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new RuntimeException("결제 검증 중 오류 발생: " + e.getMessage());
        }
    }

    private String getPortOneAccessToken() {
        Map<String, String> tokenRequest = Map.of(
                "imp_key", apiKey,
                "imp_secret", secretKey
        );

        try {
            PortOneTokenResponse response = webClient.post()
                    .uri("/users/getToken")
                    .bodyValue(tokenRequest)
                    .retrieve()
                    .bodyToMono(PortOneTokenResponse.class)
                    .block();

            if (response == null || response.getCode() != 0) {
                throw new RuntimeException("포트원 토큰 발급 실패: " +
                        (response != null ? response.getMessage() : "응답 없음"));
            }

            return response.getResponse().getAccessToken();

        } catch (Exception e) {
            log.error("포트원 토큰 발급 중 오류 발생", e);
            throw new RuntimeException("포트원 토큰 발급 실패", e);
        }
    }

    private PortOnePaymentResponse getPaymentFromPortOne(String impUid, String accessToken) {
        try {
            PortOnePaymentResponse response = webClient.get()
                    .uri("/payments/" + impUid)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(PortOnePaymentResponse.class)
                    .block();

            if (response == null || response.getCode() != 0) {
                throw new RuntimeException("포트원 결제 정보 조회 실패: " +
                        (response != null ? response.getMessage() : "응답 없음"));
            }

            return response;

        } catch (Exception e) {
            log.error("포트원 결제 정보 조회 중 오류 발생", e);
            throw new RuntimeException("포트원 결제 정보 조회 실패", e);
        }
    }

    private boolean validatePayment(Payment payment, PortOnePaymentResponse portOnePayment) {
        boolean merchantUidMatch = payment.getMerchantUid().equals(
                portOnePayment.getResponse().getMerchantUid());

        boolean amountMatch = payment.getAmount().compareTo(
                portOnePayment.getResponse().getAmount()) == 0;

        boolean statusPaid = "paid".equals(portOnePayment.getResponse().getStatus());

        log.info("결제 검증 - merchantUid 일치: {}, 금액 일치: {}, 결제 상태: {}",
                merchantUidMatch, amountMatch, portOnePayment.getResponse().getStatus());

        return merchantUidMatch && amountMatch && statusPaid;
    }

    private Payment.PaymentMethod determinePaymentMethod(String payMethod) {
        if (payMethod == null) {
            return Payment.PaymentMethod.CARD;
        }

        return switch (payMethod.toLowerCase()) {
            case "card" -> Payment.PaymentMethod.CARD;
            case "kakaopay" -> Payment.PaymentMethod.KAKAOPAY;
            case "naverpay" -> Payment.PaymentMethod.NAVERPAY;
            case "trans" -> Payment.PaymentMethod.BANK;
            default -> Payment.PaymentMethod.CARD;
        };
    }

    private String generateMerchantUid(Long reservationId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "HOTEL_" + reservationId + "_" + timestamp;
    }

    @Transactional(readOnly = true)
    public Payment findByMerchantUid(String merchantUid) {
        return paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }
}