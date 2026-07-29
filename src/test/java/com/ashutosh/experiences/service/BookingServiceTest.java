package com.ashutosh.experiences.service;

import com.ashutosh.experiences.domain.Booking;
import com.ashutosh.experiences.domain.BookingStatus;
import com.ashutosh.experiences.domain.Experience;
import com.ashutosh.experiences.domain.ExperienceFixtures;
import com.ashutosh.experiences.repository.BookingRepository;
import com.ashutosh.experiences.repository.ExperienceRepository;
import com.ashutosh.experiences.service.exception.ExperienceNotFoundException;
import com.ashutosh.experiences.service.exception.InsufficientCapacityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the capacity rule.
 *
 * No Spring context, no database. Both repositories are Mockito mocks, so these
 * tests run in milliseconds and test exactly one thing: the decision logic in
 * BookingService. If a test here fails, the bug is in that class -- not in
 * Hibernate, not in the JSON mapping, not in the HTTP layer.
 *
 * That is the trade with integration tests: these are fast and precise but
 * prove nothing about whether the pieces fit together. You want both, which is
 * why BookingControllerIT exists too.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService")
class BookingServiceTest {

    private static final Long EXPERIENCE_ID = 1L;
    private static final LocalDate TOUR_DATE = LocalDate.of(2026, 9, 1);

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private ReferenceGenerator referenceGenerator;

    @InjectMocks
    private BookingService bookingService;

    private Experience experience;

    @BeforeEach
    void setUp() {
        experience = ExperienceFixtures.withCapacity(EXPERIENCE_ID, 10);
    }

    @Nested
    @DisplayName("when creating a booking")
    class Create {

        @Test
        @DisplayName("saves the booking when there is enough room")
        void savesBookingWhenCapacityAllows() {
            given7SeatsTaken();
            when(referenceGenerator.generate()).thenReturn("VTR-TEST0001");
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Booking booking = bookingService.create(
                    EXPERIENCE_ID, "Ashutosh Sharma", "ash@example.com", 3, TOUR_DATE);

            assertThat(booking.getReference()).isEqualTo("VTR-TEST0001");
            assertThat(booking.getPartySize()).isEqualTo(3);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(booking.getExperience()).isEqualTo(experience);
        }

        /**
         * The boundary case. Capacity 10, seven seats gone, asking for exactly
         * the last three. Off-by-one errors live precisely here -- a '>=' where
         * a '>' belongs would pass every other test in this class and fail this
         * one.
         */
        @Test
        @DisplayName("allows a booking that fills the last remaining seats exactly")
        void allowsBookingThatExactlyFillsCapacity() {
            given7SeatsTaken();
            when(referenceGenerator.generate()).thenReturn("VTR-TEST0002");
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Booking booking = bookingService.create(
                    EXPERIENCE_ID, "Ashutosh Sharma", "ash@example.com", 3, TOUR_DATE);

            assertThat(booking.getPartySize()).isEqualTo(3);
        }

        @Test
        @DisplayName("rejects a booking that would exceed capacity")
        void rejectsBookingBeyondCapacity() {
            given7SeatsTaken();

            assertThatThrownBy(() -> bookingService.create(
                    EXPERIENCE_ID, "Ashutosh Sharma", "ash@example.com", 4, TOUR_DATE))
                    .isInstanceOf(InsufficientCapacityException.class)
                    .hasMessageContaining("only 3 remain");

            // The important half of this test: nothing was written.
            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("treats an empty tour as fully available")
        void handlesFirstBookingOfTheDay() {
            when(experienceRepository.findById(EXPERIENCE_ID))
                    .thenReturn(Optional.of(experience));
            when(bookingRepository.sumPartySize(EXPERIENCE_ID, TOUR_DATE, BookingStatus.CONFIRMED))
                    .thenReturn(0);
            when(referenceGenerator.generate()).thenReturn("VTR-TEST0003");
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Booking booking = bookingService.create(
                    EXPERIENCE_ID, "Ashutosh Sharma", "ash@example.com", 10, TOUR_DATE);

            assertThat(booking.getPartySize()).isEqualTo(10);
        }

        @Test
        @DisplayName("throws when the experience does not exist")
        void throwsWhenExperienceMissing() {
            when(experienceRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.create(
                    999L, "Ashutosh Sharma", "ash@example.com", 2, TOUR_DATE))
                    .isInstanceOf(ExperienceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(bookingRepository, never()).save(any(Booking.class));
        }

        /**
         * Capacity is counted per date, so a full Monday must not block Tuesday.
         * ArgumentCaptor proves the date actually reached the repository rather
         * than being dropped somewhere in the service.
         */
        @Test
        @DisplayName("counts capacity per date, not across all dates")
        void capacityIsScopedToTheRequestedDate() {
            LocalDate otherDate = TOUR_DATE.plusDays(1);
            when(experienceRepository.findById(EXPERIENCE_ID))
                    .thenReturn(Optional.of(experience));
            when(bookingRepository.sumPartySize(anyLong(), eq(otherDate), any()))
                    .thenReturn(0);
            when(referenceGenerator.generate()).thenReturn("VTR-TEST0004");
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            bookingService.create(
                    EXPERIENCE_ID, "Ashutosh Sharma", "ash@example.com", 9, otherDate);

            ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            verify(bookingRepository).sumPartySize(eq(EXPERIENCE_ID), dateCaptor.capture(), any());
            assertThat(dateCaptor.getValue()).isEqualTo(otherDate);
        }
    }

    @Nested
    @DisplayName("when checking availability")
    class Availability {

        @Test
        @DisplayName("returns capacity minus confirmed bookings")
        void returnsRemainingSeats() {
            given7SeatsTaken();

            assertThat(bookingService.remainingCapacity(EXPERIENCE_ID, TOUR_DATE)).isEqualTo(3);
        }
    }

    private void given7SeatsTaken() {
        when(experienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));
        when(bookingRepository.sumPartySize(EXPERIENCE_ID, TOUR_DATE, BookingStatus.CONFIRMED))
                .thenReturn(7);
    }
}
