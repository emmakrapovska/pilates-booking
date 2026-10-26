package com.pilates.booking.domain.dto;

import jakarta.validation.constraints.*;

public record CreateBookingRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 160) String email) {}
