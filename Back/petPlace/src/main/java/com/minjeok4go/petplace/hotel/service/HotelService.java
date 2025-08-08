package com.minjeok4go.petplace.hotel.service;

import com.minjeok4go.petplace.hotel.entity.Hotel;
import com.minjeok4go.petplace.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;

    public List<Hotel> findAvailableHotels(Hotel.PetType petType,
                                           LocalDateTime checkIn,
                                           LocalDateTime checkOut) {
        return hotelRepository.findAvailableHotels(petType, checkIn, checkOut);
    }

    public Hotel findById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("호텔을 찾을 수 없습니다."));
    }

    public List<Hotel> findAllHotels() {
        return hotelRepository.findAll();
    }
}