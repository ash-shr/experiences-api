package com.ashutosh.experiences.service;

import com.ashutosh.experiences.domain.Booking;
import com.ashutosh.experiences.domain.BookingStatus;
import com.ashutosh.experiences.domain.Experience;
import com.ashutosh.experiences.repository.BookingRepository;
import com.ashutosh.experiences.repository.ExperienceRepository;
import com.ashutosh.experiences.service.exception.BookingNotFoundException;
import com.ashutosh.experiences.service.exception.ExperienceNotFoundException;
import com.ashutosh.experiences.service.exception.InsufficientCapacityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * The only class in this project with real business logic, and therefore the
 * class worth writing unit tests against.
 *
 * The rule it enforces: an experience has a daily capacity, and the confirmed
 * bookings for a given date must never exceed it.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ExperienceRepository experienceRepository;
    private final ReferenceGenerator referenceGenerator;

    public BookingService(BookingRepository bookingRepository,
                          ExperienceRepository experienceRepository,
                          ReferenceGenerator referenceGenerator) {
        this.bookingRepository = bookingRepository;
        this.experienceRepository = experienceRepository;
        this.referenceGenerator = referenceGenerator;
    }

    /**
     * Create a booking, if there is room.
     *
     * @Transactional without readOnly: this method writes, and the capacity
     * check plus the insert need to happen in one transaction so a rollback
     * takes both with it.
     *
     * Known limitation, and I would rather say it out loud than have someone
     * find it: this is read-then-write, so two requests arriving at the same
     * moment can both read "2 seats left" and both book 2 seats. Fixing it
     * properly means either a pessimistic lock on the experience row
     * (SELECT ... FOR UPDATE), an optimistic @Version column with a retry, or a
     * database-level constraint. For a single-instance demo I have left it, but
     * it is a real race condition and it is exactly the kind of bug that only
     * shows up under load.
     */
    @Transactional
    public Booking create(Long experienceId, String travellerName, String travellerEmail,
                          int partySize, LocalDate bookingDate) {

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ExperienceNotFoundException(experienceId));

        int alreadyBooked = bookingRepository.sumPartySize(
                experienceId, bookingDate, BookingStatus.CONFIRMED);

        int remaining = experience.getDailyCapacity() - alreadyBooked;

        if (partySize > remaining) {
            log.debug("Rejecting booking for experience {} on {}: requested {}, remaining {}",
                    experienceId, bookingDate, partySize, remaining);
            throw new InsufficientCapacityException(partySize, remaining);
        }

        Booking booking = new Booking(
                referenceGenerator.generate(),
                experience,
                travellerName,
                travellerEmail,
                partySize,
                bookingDate);

        Booking saved = bookingRepository.save(booking);
        log.info("Created booking {} for experience {} on {}",
                saved.getReference(), experienceId, bookingDate);
        return saved;
    }

    public Booking findByReference(String reference) {
        return bookingRepository.findByReference(reference)
                .orElseThrow(() -> new BookingNotFoundException(reference));
    }

    /**
     * Cancelling frees the seats back up, because sumPartySize only counts
     * CONFIRMED bookings. We keep the row rather than deleting it -- you almost
     * never want to destroy transactional history.
     */
    @Transactional
    public Booking cancel(String reference) {
        Booking booking = findByReference(reference);
        booking.cancel();
        return bookingRepository.save(booking);
    }

    /**
     * Seats left on an experience for a date. Used by the controller so a
     * traveller can check before committing.
     */
    public int remainingCapacity(Long experienceId, LocalDate bookingDate) {
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ExperienceNotFoundException(experienceId));

        int alreadyBooked = bookingRepository.sumPartySize(
                experienceId, bookingDate, BookingStatus.CONFIRMED);

        return experience.getDailyCapacity() - alreadyBooked;
    }
}
