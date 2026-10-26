package com.pilates.booking.service;

import com.pilates.booking.domain.dto.CreatePilatesClassRequest;
import com.pilates.booking.domain.dto.PilatesClassResponse;
import java.util.List;

public interface PilatesClassService {

    List<PilatesClassResponse> upcoming();

    PilatesClassResponse create(CreatePilatesClassRequest req);

    void delete(Long id);
}
