package com.ashutosh.experiences.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * These run against the catalogue seeded by data.sql, so they double as a check
 * that the seed data actually loads.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/experiences")
class ExperienceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("returns the seeded catalogue")
    void returnsAllExperiences() throws Exception {
        mockMvc.perform(get("/api/experiences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].price").exists());
    }

    @Test
    @DisplayName("filters by city, case insensitively")
    void filtersByCity() throws Exception {
        mockMvc.perform(get("/api/experiences").param("city", "oxford"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[*].city", everyItem(equalToIgnoringCase("Oxford"))));
    }

    @Test
    @DisplayName("returns nothing for a city with no experiences")
    void returnsEmptyListForUnknownCity() throws Exception {
        mockMvc.perform(get("/api/experiences").param("city", "Atlantis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("returns 404 for an unknown id")
    void returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/experiences/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("reports remaining capacity for a date")
    void reportsAvailability() throws Exception {
        String date = LocalDate.now().plusDays(30).toString();

        mockMvc.perform(get("/api/experiences/{id}/availability", 1).param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experienceId").value(1))
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.remainingCapacity").value(greaterThan(0)));
    }
}
