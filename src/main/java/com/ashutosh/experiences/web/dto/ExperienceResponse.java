package com.ashutosh.experiences.web.dto;

import com.ashutosh.experiences.domain.Experience;

import java.math.BigDecimal;

/**
 * What the API returns for an experience.
 *
 * This is a Java record: an immutable data carrier where the compiler writes
 * the constructor, accessors, equals, hashCode and toString for us.
 *
 * Why have a DTO at all instead of returning the Experience entity directly?
 *  1. The entity is the database shape, the DTO is the API contract. If they
 *     are the same class, renaming a column breaks every client.
 *  2. It stops accidental leaks -- add an internal 'margin' field to the entity
 *     later and it would silently appear in the public JSON.
 *  3. Serialising an entity with lazy associations outside a transaction throws
 *     LazyInitializationException. Mapping to a DTO inside the service boundary
 *     sidesteps that whole category of bug.
 */
public record ExperienceResponse(
        Long id,
        String title,
        String city,
        String description,
        BigDecimal price,
        String currency,
        int durationMinutes,
        int dailyCapacity) {

    public static ExperienceResponse from(Experience experience) {
        return new ExperienceResponse(
                experience.getId(),
                experience.getTitle(),
                experience.getCity(),
                experience.getDescription(),
                experience.getPrice(),
                experience.getCurrency(),
                experience.getDurationMinutes(),
                experience.getDailyCapacity());
    }
}
