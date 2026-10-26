package com.pilates.booking.controller;

import com.pilates.booking.domain.dto.BookingResponse;
import com.pilates.booking.domain.dto.ConfirmationResponse;
import com.pilates.booking.domain.dto.CreateBookingRequest;
import com.pilates.booking.service.BookingService;
import com.pilates.booking.service.ConfirmationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes/{classId}/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final ConfirmationService confirmationService;

    public BookingController(BookingService bookingService, ConfirmationService confirmationService) {
        this.bookingService = bookingService;
        this.confirmationService = confirmationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@PathVariable Long classId, @Valid @RequestBody CreateBookingRequest req) {
        return bookingService.book(classId, req);
    }

    @GetMapping
    public List<BookingResponse> bookings(@PathVariable Long classId) {
        return bookingService.forClass(classId);
    }

    @GetMapping("/{bookingId}/confirmation")
    public ConfirmationResponse confirmation(@PathVariable Long classId, @PathVariable Long bookingId) {
        return confirmationService.confirmationFor(classId, bookingId);
    }
}
