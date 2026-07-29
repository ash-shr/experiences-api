package com.ashutosh.experiences.web.dto;

import com.ashutosh.experiences.domain.Booking;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Note what is NOT here: the numeric primary key. Clients address a booking by
 * its reference, which is unguessable. The internal id never leaves the server.
 */
public record BookingResponse(
        String reference,
        Long experienceId,
        String experienceTitle,
        String travellerName,
        String travellerEmail,
        int partySize,
        LocalDate bookingDate,
        String status,
        Instant createdAt) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getReference(),
                booking.getExperience().getId(),
                booking.getExperience().getTitle(),
                booking.getTravellerName(),
                booking.getTravellerEmail(),
                booking.getPartySize(),
                booking.getBookingDate(),
                booking.getStatus().name(),
                booking.getCreatedAt());
    }
}
