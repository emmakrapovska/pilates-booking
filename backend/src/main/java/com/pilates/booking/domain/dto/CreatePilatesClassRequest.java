package com.pilates.booking.domain.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreatePilatesClassRequest(
        @NotBlank @Size(max = 80) String title,
        @NotNull LocalDateTime startTime,
        @Min(15) @Max(240) int durationMinutes,
        @Min(1) @Max(30) int capacity) {}
