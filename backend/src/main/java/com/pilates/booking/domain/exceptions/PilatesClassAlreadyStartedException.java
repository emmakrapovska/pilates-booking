package com.pilates.booking.domain.exceptions;

import org.springframework.http.HttpStatus;

public class PilatesClassAlreadyStartedException extends ApiException {
    public PilatesClassAlreadyStartedException() {
        super(HttpStatus.CONFLICT, "This class has already started.");
    }
}
