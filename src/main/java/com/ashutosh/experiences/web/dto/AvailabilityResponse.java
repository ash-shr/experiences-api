package com.ashutosh.experiences.web.dto;

import java.time.LocalDate;

public record AvailabilityResponse(
        Long experienceId,
        LocalDate date,
        int remainingCapacity) {
}
