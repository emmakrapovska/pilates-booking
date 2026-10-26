package com.pilates.booking.domain.exceptions;

import org.springframework.http.HttpStatus;

public class PilatesClassStartTimeInPastException extends ApiException {
    public PilatesClassStartTimeInPastException() {
        super(HttpStatus.BAD_REQUEST, "Start time must be in the future.");
    }
}
