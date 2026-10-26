package com.pilates.booking.domain.dto;

import com.pilates.booking.domain.models.Booking;
import java.time.Instant;

public record BookingResponse(Long id, Long classId, String name, String email, Instant createdAt) {
    public static BookingResponse of(Booking b) {
        return new BookingResponse(b.getId(), b.getPilatesClass().getId(), b.getName(), b.getEmail(), b.getCreatedAt());
    }
}
