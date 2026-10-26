package com.pilates.booking.controller;

import com.pilates.booking.domain.dto.CreatePilatesClassRequest;
import com.pilates.booking.domain.dto.PilatesClassResponse;
import com.pilates.booking.service.PilatesClassService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
public class PilatesClassController {

    private final PilatesClassService classService;

    public PilatesClassController(PilatesClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public List<PilatesClassResponse> list() {
        return classService.upcoming();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PilatesClassResponse create(@Valid @RequestBody CreatePilatesClassRequest req) {
        return classService.create(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        classService.delete(id);
    }
}
