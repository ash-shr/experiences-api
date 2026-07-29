package com.ashutosh.experiences.web;

import com.ashutosh.experiences.domain.Experience;
import com.ashutosh.experiences.repository.ExperienceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the booking endpoints.
 *
 * These are the counterpart to BookingServiceTest. Nothing is mocked: a real
 * Spring context starts, a real H2 database is created, real JSON is
 * serialised, real validation runs, and the real exception handler maps errors
 * to status codes.
 *
 * They are slower, and they catch an entirely different class of bug -- a
 * missing @Valid, a DTO field that does not serialise, a JPQL query that is
 * syntactically wrong. A unit test with a mocked repository will happily pass
 * while the query underneath it is broken.
 *
 * @Transactional on the test class rolls the transaction back after each test,
 * so tests cannot leak state into each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /api/bookings")
class BookingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long experienceId;

    /**
     * Each test gets its own experience with a known, small capacity rather than
     * depending on whatever happens to be in data.sql. Tests that rely on seed
     * data break the moment someone edits the seed file.
     */
    @BeforeEach
    void setUp() {
        Experience experience = experienceRepository.save(new Experience(
                "Test Punting Tour",
                "Oxford",
                "A tour that exists only for this test.",
                new BigDecimal("32.00"),
                "GBP",
                60,
                5));
        experienceId = experience.getId();
    }

    @Test
    @DisplayName("returns 201 with a Location header when the booking succeeds")
    void createsBooking() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(experienceId, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/bookings/VTR-")))
                .andExpect(jsonPath("$.reference").value(startsWith("VTR-")))
                .andExpect(jsonPath("$.experienceId").value(experienceId))
                .andExpect(jsonPath("$.experienceTitle").value("Test Punting Tour"))
                .andExpect(jsonPath("$.partySize").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                // The internal primary key must never appear in the response.
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DisplayName("returns 409 when the tour is already full")
    void rejectsOverbooking() throws Exception {
        // Fill all 5 places.
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(experienceId, 5)))
                .andExpect(status().isCreated());

        // One more should now conflict.
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(experienceId, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Insufficient Capacity"))
                .andExpect(jsonPath("$.message").value(containsString("only 0 remain")));
    }

    @Test
    @DisplayName("frees the seats again once a booking is cancelled")
    void cancellationReleasesCapacity() throws Exception {
        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(experienceId, 5)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String reference = objectMapper.readTree(response).get("reference").asText();

        mockMvc.perform(delete("/api/bookings/{reference}", reference))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // The tour should be bookable again.
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(experienceId, 5)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("returns 400 with per-field messages when the request is invalid")
    void rejectsInvalidRequest() throws Exception {
        String invalid = objectMapper.writeValueAsString(Map.of(
                "experienceId", experienceId,
                "travellerName", "",
                "travellerEmail", "not-an-email",
                "partySize", 0,
                "bookingDate", LocalDate.now().minusDays(1).toString()));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.travellerName").exists())
                .andExpect(jsonPath("$.fieldErrors.travellerEmail").exists())
                .andExpect(jsonPath("$.fieldErrors.partySize").exists())
                .andExpect(jsonPath("$.fieldErrors.bookingDate").exists());
    }

    @Test
    @DisplayName("returns 404 when the experience does not exist")
    void rejectsUnknownExperience() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(999_999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("returns 404 for an unknown booking reference")
    void returnsNotFoundForUnknownReference() throws Exception {
        mockMvc.perform(get("/api/bookings/{reference}", "VTR-NOPE0000"))
                .andExpect(status().isNotFound());
    }

    private String bookingJson(Long experienceId, int partySize) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "experienceId", experienceId,
                "travellerName", "Ashutosh Sharma",
                "travellerEmail", "ash@example.com",
                "partySize", partySize,
                "bookingDate", LocalDate.now().plusDays(30).toString()));
    }
}
