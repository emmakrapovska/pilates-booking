package com.pilates.booking.service;

import com.pilates.booking.domain.dto.BookingResponse;
import com.pilates.booking.domain.dto.CreateBookingRequest;
import java.util.List;

public interface BookingService {

    BookingResponse book(Long classId, CreateBookingRequest req);

    List<BookingResponse> forClass(Long classId);
}
