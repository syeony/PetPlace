package com.minjeok4go.petplace.scheduler;

import com.minjeok4go.petplace.hotel.entity.Reservation;
import com.minjeok4go.petplace.hotel.service.ReservationService;
import com.minjeok4go.petplace.notification.service.FCMNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationReminderScheduler {
    
    private final ReservationService reservationService;
    private final FCMNotificationService FCMNotificationService;
    
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시 실행
    public void sendReservationReminders() {
        log.info("예약 리마인더 스케줄러 실행 시작");
        
        try {
            // 내일 체크인 예정인 예약 조회 (오늘 09:00 ~ 내일 08:59)
            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusDays(2).minusSeconds(1);
            
            List<Reservation> upcomingReservations = 
                reservationService.findUpcomingReservations(start, end);
            
            log.info("리마인더 대상 예약 수: {}", upcomingReservations.size());
            
            for (Reservation reservation : upcomingReservations) {
                FCMNotificationService.sendReservationReminderNotification(reservation);
            }
            
            log.info("예약 리마인더 스케줄러 실행 완료");
            
        } catch (Exception e) {
            log.error("예약 리마인더 스케줄러 실행 중 오류 발생", e);
        }
    }
}
