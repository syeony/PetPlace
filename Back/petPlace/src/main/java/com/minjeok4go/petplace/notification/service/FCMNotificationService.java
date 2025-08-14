// File: src/main/java/com/minjeok4go/petplace/notification/service/NotificationService.java
package com.minjeok4go.petplace.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    // FCM 토큰 관리를 위한 서비스 (기존 User 관련 서비스 활용)
    // private final UserService userService;

    public void sendPaymentSuccessNotification(Payment payment) {
        try {
            // 사용자의 FCM 토큰 조회 (기존 User 엔티티에 fcmToken 필드 추가 필요)
            String fcmToken = getFcmTokenByUserId(payment.getReservationId());

            if (fcmToken != null && !fcmToken.isEmpty()) {
                Notification notification = Notification.builder()
                        .setTitle("결제 완료")
                        .setBody("호텔 예약이 성공적으로 완료되었습니다!")
                        .build();

                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .putData("type", "PAYMENT_SUCCESS")
                        .putData("reservationId", payment.getReservationId().toString())
                        .build();

                String response = firebaseMessaging.send(message);
                log.info("결제 완료 알림 전송 성공: {}", response);
            }
        } catch (Exception e) {
            log.error("결제 완료 알림 전송 실패", e);
        }
    }

    public void sendReservationReminderNotification(Reservation reservation) {
        try {
            String fcmToken = getFcmTokenByUserId(reservation.getUserId());

            if (fcmToken != null && !fcmToken.isEmpty()) {
                // ⭐ 수정: 새로운 방식으로 체크인 날짜 조회
                LocalDate checkInDate = reservation.getCheckInDate();

                String message = "";
                if (checkInDate != null) {
                    String formattedDate = checkInDate.format(DateTimeFormatter.ofPattern("MM월 dd일"));
                    message = String.format("내일(%s) 호텔 체크인 예정입니다!", formattedDate);
                } else {
                    message = "호텔 예약 리마인더: 예약된 날짜를 확인해주세요!";
                }

                Notification notification = Notification.builder()
                        .setTitle("호텔 예약 리마인더")
                        .setBody(message)
                        .build();

                Message fcmMessage = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .putData("type", "RESERVATION_REMINDER")
                        .putData("reservationId", reservation.getId().toString())
                        .putData("checkInDate", checkInDate != null ? checkInDate.toString() : "")
                        .build();

                String response = firebaseMessaging.send(fcmMessage);
                log.info("예약 리마인더 알림 전송 성공: {}", response);
            }
        } catch (Exception e) {
            log.error("예약 리마인더 알림 전송 실패", e);
        }
    }

    /**
     * 예약 취소 알림 (새로 추가)
     */
    public void sendReservationCancelledNotification(Reservation reservation) {
        try {
            String fcmToken = getFcmTokenByUserId(reservation.getUserId());

            if (fcmToken != null && !fcmToken.isEmpty()) {
                LocalDate checkInDate = reservation.getCheckInDate();
                LocalDate checkOutDate = reservation.getCheckOutDate();

                String dateRange = "";
                if (checkInDate != null && checkOutDate != null) {
                    if (checkInDate.equals(checkOutDate)) {
                        dateRange = checkInDate.format(DateTimeFormatter.ofPattern("MM월 dd일"));
                    } else {
                        dateRange = String.format("%s ~ %s",
                                checkInDate.format(DateTimeFormatter.ofPattern("MM월 dd일")),
                                checkOutDate.format(DateTimeFormatter.ofPattern("MM월 dd일")));
                    }
                }

                Notification notification = Notification.builder()
                        .setTitle("예약 취소 완료")
                        .setBody(String.format("호텔 예약이 취소되었습니다. (%s)", dateRange))
                        .build();

                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .putData("type", "RESERVATION_CANCELLED")
                        .putData("reservationId", reservation.getId().toString())
                        .build();

                String response = firebaseMessaging.send(message);
                log.info("예약 취소 알림 전송 성공: {}", response);
            }
        } catch (Exception e) {
            log.error("예약 취소 알림 전송 실패", e);
        }
    }

    /**
     * 예약 확정 알림 (새로 추가)
     */
    public void sendReservationConfirmedNotification(Reservation reservation) {
        try {
            String fcmToken = getFcmTokenByUserId(reservation.getUserId());

            if (fcmToken != null && !fcmToken.isEmpty()) {
                LocalDate checkInDate = reservation.getCheckInDate();
                String hotelName = reservation.getHotel() != null ?
                        reservation.getHotel().getName() : "호텔";

                String message = checkInDate != null ?
                        String.format("%s 예약이 확정되었습니다! 체크인: %s",
                                hotelName,
                                checkInDate.format(DateTimeFormatter.ofPattern("MM월 dd일"))) :
                        String.format("%s 예약이 확정되었습니다!", hotelName);

                Notification notification = Notification.builder()
                        .setTitle("예약 확정")
                        .setBody(message)
                        .build();

                Message fcmMessage = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(notification)
                        .putData("type", "RESERVATION_CONFIRMED")
                        .putData("reservationId", reservation.getId().toString())
                        .putData("hotelName", hotelName)
                        .build();

                String response = firebaseMessaging.send(fcmMessage);
                log.info("예약 확정 알림 전송 성공: {}", response);
            }
        } catch (Exception e) {
            log.error("예약 확정 알림 전송 실패", e);
        }
    }

    // FCM 토큰 조회 (기존 User 서비스와 연동 필요)
    private String getFcmTokenByUserId(Long userId) {
        // 기존 UserService를 활용하여 FCM 토큰 조회
        // return userService.getFcmTokenByUserId(userId);
        return null; // 임시값 - 실제 구현 시 수정 필요
    }
}