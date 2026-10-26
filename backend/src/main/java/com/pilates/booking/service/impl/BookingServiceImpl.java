package com.pilates.booking.service.impl;

import com.pilates.booking.domain.dto.BookingResponse;
import com.pilates.booking.domain.dto.CreateBookingRequest;
import com.pilates.booking.domain.exceptions.DuplicateBookingException;
import com.pilates.booking.domain.exceptions.PilatesClassAlreadyStartedException;
import com.pilates.booking.domain.exceptions.PilatesClassFullyBookedException;
import com.pilates.booking.domain.exceptions.PilatesClassNotFoundException;
import com.pilates.booking.domain.models.Booking;
import com.pilates.booking.domain.models.PilatesClass;
import com.pilates.booking.repository.BookingRepository;
import com.pilates.booking.repository.PilatesClassRepository;
import com.pilates.booking.service.BookingService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private final PilatesClassRepository classes;
    private final BookingRepository bookings;
    private final Clock clock;

    public BookingServiceImpl(PilatesClassRepository classes, BookingRepository bookings, Clock clock) {
        this.classes = classes;
        this.bookings = bookings;
        this.clock = clock;
    }

    @Transactional
    @Override
    public BookingResponse book(Long classId, CreateBookingRequest req) {
        PilatesClass pilatesClass = classes.findByIdForUpdate(classId)
                .orElseThrow(PilatesClassNotFoundException::new);

        if (!pilatesClass.getStartTime().isAfter(LocalDateTime.now(clock))) {
            throw new PilatesClassAlreadyStartedException();
        }
        String email = req.email().trim().toLowerCase();
        if (bookings.existsByPilatesClassIdAndEmailIgnoreCase(classId, email)) {
            throw new DuplicateBookingException();
        }
        if (bookings.countByPilatesClassId(classId) >= pilatesClass.getCapacity()) {
            throw new PilatesClassFullyBookedException();
        }
        return BookingResponse.of(bookings.save(new Booking(pilatesClass, req.name().trim(), email)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingResponse> forClass(Long classId) {
        if (!classes.existsById(classId)) {
            throw new PilatesClassNotFoundException();
        }
        return bookings.findByPilatesClassIdOrderByCreatedAtAsc(classId).stream().map(BookingResponse::of).toList();
    }
}
