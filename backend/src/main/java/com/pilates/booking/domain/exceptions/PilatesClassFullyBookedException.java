package com.pilates.booking.domain.exceptions;

import org.springframework.http.HttpStatus;

public class PilatesClassFullyBookedException extends ApiException {
    public PilatesClassFullyBookedException() {
        super(HttpStatus.CONFLICT, "Sorry, this class just got fully booked.");
    }
}
