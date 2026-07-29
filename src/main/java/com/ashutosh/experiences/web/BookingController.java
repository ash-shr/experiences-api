package com.ashutosh.experiences.web;

import com.ashutosh.experiences.domain.Booking;
import com.ashutosh.experiences.service.BookingService;
import com.ashutosh.experiences.web.dto.BookingResponse;
import com.ashutosh.experiences.web.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /api/bookings
     *
     * @Valid is what activates the constraints on CreateBookingRequest. Without
     * it the annotations are decoration and nothing is checked.
     *
     * Returns 201 Created with a Location header pointing at the new resource,
     * rather than a bare 200. That is what the HTTP spec asks for on creation
     * and it means a client does not have to construct the URL itself.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest request,
            UriComponentsBuilder uriBuilder) {

        Booking booking = bookingService.create(
                request.experienceId(),
                request.travellerName(),
                request.travellerEmail(),
                request.partySize(),
                request.bookingDate());

        URI location = uriBuilder
                .path("/api/bookings/{reference}")
                .buildAndExpand(booking.getReference())
                .toUri();

        return ResponseEntity.created(location).body(BookingResponse.from(booking));
    }

    @GetMapping("/{reference}")
    public BookingResponse findByReference(@PathVariable String reference) {
        return BookingResponse.from(bookingService.findByReference(reference));
    }

    /**
     * DELETE /api/bookings/VTR-A1B2C3D4
     *
     * Soft cancel, not a row delete. Returns the cancelled booking so the client
     * can see the new status without a follow-up request.
     */
    @DeleteMapping("/{reference}")
    public BookingResponse cancel(@PathVariable String reference) {
        return BookingResponse.from(bookingService.cancel(reference));
    }
}
