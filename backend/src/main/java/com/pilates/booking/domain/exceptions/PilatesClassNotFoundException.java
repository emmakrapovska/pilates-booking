package com.pilates.booking.domain.exceptions;

import org.springframework.http.HttpStatus;

public class PilatesClassNotFoundException extends ApiException {
    public PilatesClassNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Class not found.");
    }
}
