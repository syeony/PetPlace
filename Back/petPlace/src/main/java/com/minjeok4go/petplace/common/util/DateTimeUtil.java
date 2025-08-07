package com.minjeok4go.petplace.common.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    public static boolean isValidReservationPeriod(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            return false;
        }

        // 체크인이 현재 시간보다 미래여야 함
        if (checkIn.isBefore(LocalDateTime.now())) {
            return false;
        }

        // 체크아웃이 체크인보다 늦어야 함
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            return false;
        }

        // 최대 30일까지만 예약 가능
        long daysBetween = ChronoUnit.DAYS.between(checkIn, checkOut);
        return daysBetween <= 30;
    }

    public static long calculateNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        return ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
    }
}
