package com.ashutosh.experiences.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A traveller's booking against an Experience on a specific date.
 *
 * Design notes:
 *  - 'reference' is the customer-facing identifier (VTR-A1B2C3D4). The numeric
 *    primary key is never exposed over the API. Sequential integer ids in URLs
 *    let anyone enumerate every booking in the system.
 *  - fetch = LAZY on the experience. With the default EAGER, every query that
 *    loads bookings also joins and loads experiences whether we need them or
 *    not, which is the classic source of N+1 query problems.
 *  - bookingDate is a LocalDate, not an Instant: a tour on the 14th of August
 *    is on the 14th of August regardless of the traveller's time zone.
 *  - createdAt is an Instant because that IS a point on the global timeline.
 */
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;

    @Column(nullable = false)
    private String travellerName;

    @Column(nullable = false)
    private String travellerEmail;

    @Column(nullable = false)
    private int partySize;

    @Column(nullable = false)
    private LocalDate bookingDate;

    /**
     * EnumType.STRING, not ORDINAL. ORDINAL stores the enum's position as an
     * integer, so reordering the enum silently corrupts every existing row.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Booking() {
    }

    public Booking(String reference, Experience experience, String travellerName,
                   String travellerEmail, int partySize, LocalDate bookingDate) {
        this.reference = reference;
        this.experience = experience;
        this.travellerName = travellerName;
        this.travellerEmail = travellerEmail;
        this.partySize = partySize;
        this.bookingDate = bookingDate;
        this.status = BookingStatus.CONFIRMED;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Experience getExperience() {
        return experience;
    }

    public String getTravellerName() {
        return travellerName;
    }

    public String getTravellerEmail() {
        return travellerEmail;
    }

    public int getPartySize() {
        return partySize;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
