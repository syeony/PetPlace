package com.minjeok4go.petplace.hotel.exception;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(String message) {
        super(message);
    }

    public HotelNotFoundException(Long hotelId) {
        super("호텔을 찾을 수 없습니다. ID: " + hotelId);
    }
}
