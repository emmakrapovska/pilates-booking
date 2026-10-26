package com.pilates.booking.service;

import com.pilates.booking.domain.dto.ConfirmationResponse;

public interface ConfirmationService {

    ConfirmationResponse confirmationFor(Long classId, Long bookingId);
}
