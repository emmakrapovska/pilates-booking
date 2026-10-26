package com.pilates.booking.domain.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pilates_class_id", "email"}))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pilates_class_id")
    private PilatesClass pilatesClass;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Booking() {}

    public Booking(PilatesClass pilatesClass, String name, String email) {
        this.pilatesClass = pilatesClass;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public PilatesClass getPilatesClass() { return pilatesClass; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }
}
