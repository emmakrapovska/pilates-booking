package com.pilates.booking.service.impl;

import com.pilates.booking.domain.dto.CreatePilatesClassRequest;
import com.pilates.booking.domain.dto.PilatesClassResponse;
import com.pilates.booking.domain.exceptions.PilatesClassNotFoundException;
import com.pilates.booking.domain.exceptions.PilatesClassStartTimeInPastException;
import com.pilates.booking.domain.models.PilatesClass;
import com.pilates.booking.repository.BookingRepository;
import com.pilates.booking.repository.PilatesClassRepository;
import com.pilates.booking.service.PilatesClassService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PilatesClassServiceImpl implements PilatesClassService {

    private final PilatesClassRepository classes;
    private final BookingRepository bookings;
    private final Clock clock;

    public PilatesClassServiceImpl(PilatesClassRepository classes, BookingRepository bookings, Clock clock) {
        this.classes = classes;
        this.bookings = bookings;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PilatesClassResponse> upcoming() {
        List<PilatesClass> list = classes.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now(clock));
        Map<Long, Long> counts = new HashMap<>();
        if (!list.isEmpty()) {
            bookings.countForClasses(list.stream().map(PilatesClass::getId).toList())
                    .forEach(row -> counts.put(row.getClassId(), row.getCnt()));
        }
        return list.stream().map(c -> PilatesClassResponse.of(c, counts.getOrDefault(c.getId(), 0L))).toList();
    }

    @Transactional
    @Override
    public PilatesClassResponse create(CreatePilatesClassRequest req) {
        if (!req.startTime().isAfter(LocalDateTime.now(clock))) {
            throw new PilatesClassStartTimeInPastException();
        }
        PilatesClass saved = classes.save(new PilatesClass(req.title().trim(), req.startTime(), req.durationMinutes(), req.capacity()));
        return PilatesClassResponse.of(saved, 0);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!classes.existsById(id)) {
            throw new PilatesClassNotFoundException();
        }
        bookings.deleteByPilatesClassId(id);
        classes.deleteById(id);
    }
}
