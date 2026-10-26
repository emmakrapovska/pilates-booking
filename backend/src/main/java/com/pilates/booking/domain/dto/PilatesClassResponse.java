package com.pilates.booking.domain.dto;

import com.pilates.booking.domain.models.PilatesClass;
import java.time.LocalDateTime;

public record PilatesClassResponse(
        Long id,
        String title,
        LocalDateTime startTime,
        int durationMinutes,
        int capacity,
        int booked,
        int spotsLeft,
        boolean available) {

    public static PilatesClassResponse of(PilatesClass c, long booked) {
        int left = Math.max(0, c.getCapacity() - (int) booked);
        return new PilatesClassResponse(c.getId(), c.getTitle(), c.getStartTime(),
                c.getDurationMinutes(), c.getCapacity(), (int) booked, left, left > 0);
    }
}
