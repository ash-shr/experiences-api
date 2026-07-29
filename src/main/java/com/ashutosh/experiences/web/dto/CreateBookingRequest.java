package com.ashutosh.experiences.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Incoming booking request.
 *
 * The constraints here are the first line of defence. Combined with @Valid on
 * the controller method, Spring rejects a malformed request with a 400 before
 * any of our code runs -- no null checks scattered through the service.
 *
 * Note these validate the SHAPE of the request only. Whether there is actually
 * room on the tour is a business rule, and business rules belong in the
 * service, not in annotations on a DTO.
 */
public record CreateBookingRequest(

        @NotNull(message = "experienceId is required")
        Long experienceId,

        @NotBlank(message = "travellerName is required")
        String travellerName,

        @NotBlank(message = "travellerEmail is required")
        @Email(message = "travellerEmail must be a valid email address")
        String travellerEmail,

        @Min(value = 1, message = "partySize must be at least 1")
        @Max(value = 50, message = "partySize cannot exceed 50")
        int partySize,

        @NotNull(message = "bookingDate is required")
        @Future(message = "bookingDate must be in the future")
        LocalDate bookingDate) {
}
