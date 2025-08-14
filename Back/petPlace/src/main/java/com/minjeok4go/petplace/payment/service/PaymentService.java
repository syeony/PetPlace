package com.minjeok4go.petplace.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.hotel.service.ReservationService;
import com.minjeok4go.petplace.notification.service.FCMNotificationService;
import com.minjeok4go.petplace.payment.dto.*; // 모든 DTO 임포트
import com.minjeok4go.petplace.payment.entity.Payment;
import com.minjeok4go.petplace.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.minjeok4go.petplace.common.util.PaymentUtil.generateMerchantUid;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationService reservationService;
    private final FCMNotificationService FCMNotificationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper를 Bean으로 주입받거나 필드로 생성

    @Value("${portone.base-url}")
    private String portoneBaseUrl; // 포트원 V1, V2 API URL이 다를 수 있으므로 명확히 분리

    @Value("${portone.v2.base-url}")
    private String portoneV2BaseUrl;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.secret-key}")
    private String secretKey;

    @Value("${portone.v2.api-token}")
    private String v2ApiToken;

    @Value("${portone.webhook.secret}")
    private String webhookSecret;

    // =================================================================
    // == Public Methods (Controller에서 호출하는 진입점)
    // =================================================================

    @Transactional
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

    @Transactional(readOnly = true)
    public Payment findByMerchantUid(String merchantUid) {
        return paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다. merchantUid: " + merchantUid));
    }

    public void handleWebhook(String webhookId, String webhookSignature, String webhookTimestamp, String payload) {
        log.info("웹훅 처리 시작 - webhookId: {}", webhookId);
        verifyWebhookSignature(webhookId, webhookSignature, webhookTimestamp, payload); // 1. 시그니처 검증
        PortoneWebhookRequest webhookData = parseWebhookPayload(payload); // 2. 페이로드 파싱
        handleWebhookEvent(webhookData); // 3. 이벤트 처리
    }

    // =================================================================
    // == Webhook Internal Logic (웹훅 처리 내부 로직)
    // =================================================================

    private void verifyWebhookSignature(String webhookId, String webhookSignature, String webhookTimestamp, String payload) {
        // 개발 환경에서는 시그니처 검증 완전 스킵
        log.warn("개발 환경 - 시그니처 검증 스킵: {}", webhookId);
        return;
        
        // 운영 환경에서만 아래 코드 사용
        /*
        long now = System.currentTimeMillis() / 1000;
        long requestTimestamp = Long.parseLong(webhookTimestamp);
        if (Math.abs(now - requestTimestamp) > 300) { // 5분 이내 요청만 유효
            throw new IllegalArgumentException("유효하지 않은 타임스탬프: " + webhookTimestamp);
        }

        try {
            String dataToSign = String.join(".", webhookId, webhookTimestamp, payload);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256"));
            String expectedSignature = Base64.getEncoder().encodeToString(mac.doFinal(dataToSign.getBytes()));

            if (!expectedSignature.equals(webhookSignature)) {
                throw new IllegalArgumentException("유효하지 않은 시그니처");
            }
            log.info("웹훅 시그니처 검증 성공 - webhookId: {}", webhookId);
        } catch (Exception e) {
            throw new RuntimeException("시그니처 검증 중 오류 발생", e);
        }
        */
    }

    private PortoneWebhookRequest parseWebhookPayload(String payload) {
        try {
            return objectMapper.readValue(payload, PortoneWebhookRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("웹훅 페이로드 파싱 실패", e);
        }
    }

    private void handleWebhookEvent(PortoneWebhookRequest webhookData) {
        String eventType = webhookData.getType();
        String paymentId = webhookData.getData().getPaymentId();

        log.info("웹훅 이벤트 처리 - type: {}, paymentId: {}", eventType, paymentId);

        switch (eventType) {
            case "Transaction.Paid" -> processPaidPayment(paymentId);
            case "Transaction.Cancelled" -> processCancelledPayment(paymentId);
            // case "Transaction.Failed" -> processFailedPayment(paymentId);
            default -> log.warn("처리하지 않는 웹훅 이벤트 타입: {}", eventType);
        }
    }

    @Transactional
    public void processPaidPayment(String paymentId) {
        log.info("결제 완료 웹훅 처리 시작 - paymentId: {}", paymentId);

        // 1. 멱등성 체크: 이미 처리된 결제인지 확인
        if (paymentRepository.findByImpUid(paymentId).isPresent()) {
            log.warn("이미 처리된 결제입니다 (멱등성) - paymentId: {}", paymentId);
            return;
        }

        // 개발/테스트 환경: manual_payment로 시작하는 경우 포트원 API 호출 스킵
        if (paymentId.startsWith("manual_payment")) {
            log.warn("개발 환경 - 포트원 API 호출 스킵하고 수동 결제 처리: {}", paymentId);
            processManualPayment(paymentId);
            return;
        }

        // 2. 포트원 V2 API로 결제 정보 재조회
        PortoneV2PaymentResponse portonePayment = getPaymentFromPortOneV2(paymentId);

        // 3. customData에서 merchantUid 추출하여 우리 DB의 Payment 정보 조회
        String merchantUid = extractMerchantUidFromCustomData(portonePayment.getCustomData());
        Payment payment = findByMerchantUid(merchantUid);

        // 4. 결제 정보 교차 검증
        validatePaymentFromWebhook(payment, portonePayment);

        // 5. DB에 최종 결제 정보 업데이트 (상태: PAID)
        payment.setImpUid(paymentId);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentMethod(determinePaymentMethodFromV2(portonePayment.getMethod()));
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 6. 예약 확정 처리
        reservationService.confirmReservation(payment.getReservationId());

        // 7. 알림 전송
        FCMNotificationService.sendPaymentSuccessNotification(payment);

        log.info("웹훅 결제 완료 처리 성공 - paymentId: {}, merchantUid: {}", paymentId, merchantUid);
    }

    /**
     * 개발/테스트용 수동 결제 처리 메서드
     */
    @Transactional
    public void processManualPayment(String paymentId) {
        // paymentId에서 merchantUid 추출 (예: manual_payment_HOTEL_27 -> HOTEL_27_날짜)
        String hotelPrefix = paymentId.replace("manual_payment_", "");
        
        // 최근 PENDING 상태의 Payment 중에서 호텔 예약 찾기
        Payment payment = paymentRepository.findTopByStatusAndMerchantUidContainingOrderByCreatedAtDesc(
                Payment.PaymentStatus.PENDING, hotelPrefix)
                .orElseThrow(() -> new IllegalArgumentException("처리할 PENDING 결제를 찾을 수 없습니다: " + hotelPrefix));

        log.info("개발 환경 수동 결제 처리 - merchantUid: {}, amount: {}", payment.getMerchantUid(), payment.getAmount());

        // DB에 최종 결제 정보 업데이트 (상태: PAID)
        payment.setImpUid(paymentId);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentMethod(Payment.PaymentMethod.CARD); // 테스트용 기본값
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 예약 확정 처리
        reservationService.confirmReservation(payment.getReservationId());

        log.info("개발 환경 수동 결제 완료 - paymentId: {}, merchantUid: {}", paymentId, payment.getMerchantUid());
    }

    @Transactional
    public void processCancelledPayment(String paymentId) {
        log.info("결제 취소 웹훅 처리 시작 - paymentId: {}", paymentId);

        Payment payment = paymentRepository.findByImpUid(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("취소할 결제 정보를 찾을 수 없습니다. paymentId: " + paymentId));

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        reservationService.cancelReservationByPayment(payment.getReservationId());
        log.info("웹훅 결제 취소 처리 성공 - paymentId: {}", paymentId);
    }

    private PortoneV2PaymentResponse getPaymentFromPortOneV2(String paymentId) {
        String url = portoneV2BaseUrl + "/payments/" + paymentId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + v2ApiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PortoneV2PaymentResponse> responseEntity =
                    restTemplate.exchange(url, HttpMethod.GET, entity, PortoneV2PaymentResponse.class);
            return Objects.requireNonNull(responseEntity.getBody());
        } catch (RestClientException e) {
            throw new RuntimeException("포트원 V2 결제 정보 조회 실패", e);
        }
    }

    private void validatePaymentFromWebhook(Payment payment, PortoneV2PaymentResponse portonePayment) {
        BigDecimal portoneAmount = new BigDecimal(portonePayment.getAmount().getTotal());
        if (payment.getAmount().compareTo(portoneAmount) != 0) {
            throw new IllegalStateException("결제 금액 불일치 - DB: " + payment.getAmount() + ", 포트원: " + portoneAmount);
        }
        if (!"PAID".equals(portonePayment.getStatus())) {
            throw new IllegalStateException("결제 상태가 PAID가 아님: " + portonePayment.getStatus());
        }
    }

    private String extractMerchantUidFromCustomData(String customData) {
        if (customData == null) {
            throw new IllegalArgumentException("customData가 비어있어 merchantUid를 추출할 수 없습니다.");
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(customData);
            if (jsonNode.has("merchantUid")) {
                return jsonNode.get("merchantUid").asText();
            }
            throw new IllegalArgumentException("customData에 merchantUid 필드가 없습니다.");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("customData 파싱 실패", e);
        }
    }

    private Payment.PaymentMethod determinePaymentMethodFromV2(PortoneV2PaymentResponse.PaymentMethodInfo method) {
        if (method == null || method.getType() == null) return Payment.PaymentMethod.ETC;
        return switch (method.getType().toLowerCase()) {
            case "card" -> Payment.PaymentMethod.CARD;
            case "easy_pay" -> {
                if (method.getEasyPay() != null) {
                    String provider = method.getEasyPay().getProvider();
                    if ("KAKAOPAY".equals(provider)) yield Payment.PaymentMethod.KAKAOPAY;
                    if ("NAVERPAY".equals(provider)) yield Payment.PaymentMethod.NAVERPAY;
                }
                yield Payment.PaymentMethod.ETC;
            }
            case "transfer" -> Payment.PaymentMethod.BANK;
            default -> Payment.PaymentMethod.ETC;
        };
    }

    /**
     * 포트원 V1 형식 결제 완료 처리
     */
    @Transactional
    public void processV1PaidPayment(String impUid, String merchantUid) {
        log.info("V1 결제 완료 웹훅 처리 시작 - impUid: {}, merchantUid: {}", impUid, merchantUid);

        // 1. 멱등성 체크
        if (paymentRepository.findByImpUid(impUid).isPresent()) {
            log.warn("이미 처리된 결제입니다 (V1 멱등성) - impUid: {}", impUid);
            return;
        }

        // 2. merchantUid로 우리 DB의 Payment 정보 조회
        Payment payment = findByMerchantUid(merchantUid);

        // 3. DB에 최종 결제 정보 업데이트 (상태: PAID)
        payment.setImpUid(impUid);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentMethod(Payment.PaymentMethod.KAKAOPAY); // V1에서는 카카오페이로 추정
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 4. 예약 확정 처리
        reservationService.confirmReservation(payment.getReservationId());

        // 5. 알림 전송
        FCMNotificationService.sendPaymentSuccessNotification(payment);

        log.info("V1 웹훅 결제 완료 처리 성공 - impUid: {}, merchantUid: {}", impUid, merchantUid);
    }

    /**
     * 포트원 V1 형식 결제 취소 처리
     */
    @Transactional
    public void processV1CancelledPayment(String impUid, String merchantUid) {
        log.info("V1 결제 취소 웹훅 처리 시작 - impUid: {}, merchantUid: {}", impUid, merchantUid);

        Payment payment = paymentRepository.findByImpUid(impUid)
                .orElseThrow(() -> new IllegalArgumentException("취소할 결제 정보를 찾을 수 없습니다. impUid: " + impUid));

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        reservationService.cancelReservationByPayment(payment.getReservationId());
        log.info("V1 웹훅 결제 취소 처리 성공 - impUid: {}", impUid);
    }

    // =================================================================
    // == Legacy Methods (더 이상 사용되지 않는 레거시 메소드들)
    // =================================================================
    /*
     * 아래의 verifyAndCompletePayment, getPortOneAccessToken, getPaymentFromPortOne 등은
     * V1 클라이언트 검증 방식에서 사용되던 메소드들입니다.
     * V2 웹훅 방식에서는 더 이상 필요하지 않으므로 삭제하거나 주석 처리하는 것을 권장합니다.
     */
}