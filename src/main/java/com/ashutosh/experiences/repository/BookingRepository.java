package com.ashutosh.experiences.repository;

import com.ashutosh.experiences.domain.Booking;
import com.ashutosh.experiences.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReference(String reference);

    /**
     * How many seats are already taken on this experience on this date.
     *
     * Written as an explicit JPQL query rather than loading every booking and
     * summing in Java: the database can answer this without shipping the rows
     * over the wire, and it stays fast as the table grows.
     *
     * COALESCE matters. SUM over zero rows returns NULL in SQL, not 0, and
     * unboxing a null Integer into an int throws NullPointerException. This is
     * the kind of thing that works perfectly until the first booking of the day.
     */
    @Query("""
            SELECT COALESCE(SUM(b.partySize), 0)
            FROM Booking b
            WHERE b.experience.id = :experienceId
              AND b.bookingDate = :bookingDate
              AND b.status = :status
            """)
    int sumPartySize(@Param("experienceId") Long experienceId,
                     @Param("bookingDate") LocalDate bookingDate,
                     @Param("status") BookingStatus status);
}
