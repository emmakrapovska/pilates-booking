package com.pilates.booking.domain.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateBookingException extends ApiException {
    public DuplicateBookingException() {
        super(HttpStatus.CONFLICT, "You have already booked this class with this email.");
    }
}
