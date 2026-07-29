package com.ashutosh.experiences.service;

import com.ashutosh.experiences.domain.Experience;
import com.ashutosh.experiences.repository.ExperienceRepository;
import com.ashutosh.experiences.service.exception.ExperienceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    /**
     * Constructor injection, not @Autowired on fields. Two reasons: the
     * dependency can be final (so the object cannot exist in a half-built
     * state), and a unit test can construct this class with a mock directly
     * rather than needing a Spring context to inject one.
     */
    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    /**
     * Search with two optional filters.
     *
     * Honest note on this method: the city filter is pushed down into the
     * database, but the price filter is applied in memory with a stream. That
     * is fine for a seeded catalogue of seven rows and wrong at real scale --
     * you would be loading every experience in a city just to discard most of
     * them. The proper fix is a JPA Specification or a Criteria query that
     * builds one WHERE clause containing both predicates. I left it simple
     * deliberately rather than pretending a demo needs a query DSL.
     */
    public List<Experience> search(String city, BigDecimal maxPrice) {
        List<Experience> results = (city == null || city.isBlank())
                ? experienceRepository.findAll()
                : experienceRepository.findByCityIgnoreCase(city);

        if (maxPrice == null) {
            return results;
        }

        return results.stream()
                .filter(experience -> experience.getPrice().compareTo(maxPrice) <= 0)
                .toList();
    }

    public Experience findById(Long id) {
        return experienceRepository.findById(id)
                .orElseThrow(() -> new ExperienceNotFoundException(id));
    }
}
