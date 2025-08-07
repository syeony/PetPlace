package com.minjeok4go.petplace.hotel.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(Long reservationId) {
        super("예약을 찾을 수 없습니다. ID: " + reservationId);
    }
}
