package com.ashutosh.experiences.web;

import com.ashutosh.experiences.service.BookingService;
import com.ashutosh.experiences.service.ExperienceService;
import com.ashutosh.experiences.web.dto.AvailabilityResponse;
import com.ashutosh.experiences.web.dto.ExperienceResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controllers stay thin on purpose: translate HTTP to a service call, translate
 * the result back to a DTO. No business logic lives here. That separation is
 * what lets the interesting logic be unit tested without spinning up a web
 * server.
 */
@RestController
@RequestMapping("/api/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;
    private final BookingService bookingService;

    public ExperienceController(ExperienceService experienceService,
                                BookingService bookingService) {
        this.experienceService = experienceService;
        this.bookingService = bookingService;
    }

    /**
     * GET /api/experiences?city=Oxford&maxPrice=30
     * Both parameters optional.
     */
    @GetMapping
    public List<ExperienceResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return experienceService.search(city, maxPrice)
                .stream()
                .map(ExperienceResponse::from)
                .toList();
    }

    /**
     * GET /api/experiences/1
     *
     * Returns 404 if the id does not exist -- see GlobalExceptionHandler. The
     * controller does not need a null check because the service throws.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ExperienceResponse.from(experienceService.findById(id)));
    }

    /**
     * GET /api/experiences/1/availability?date=2026-09-01
     *
     * @DateTimeFormat tells Spring how to parse the query string into a
     * LocalDate. ISO is the default for LocalDate in Boot 3, but stating it
     * makes the contract explicit to anyone reading the class.
     */
    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return new AvailabilityResponse(id, date, bookingService.remainingCapacity(id, date));
    }
}
