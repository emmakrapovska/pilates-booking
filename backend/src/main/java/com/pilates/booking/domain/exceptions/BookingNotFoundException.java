package com.pilates.booking.domain.exceptions;

import org.springframework.http.HttpStatus;

public class BookingNotFoundException extends ApiException {
    public BookingNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Booking not found.");
    }
}
