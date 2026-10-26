package com.pilates.booking.domain.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pilates_classes")
public class PilatesClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int capacity;

    protected PilatesClass() {}

    public PilatesClass(String title, LocalDateTime startTime, int durationMinutes, int capacity) {
        this.title = title;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getCapacity() { return capacity; }
}
