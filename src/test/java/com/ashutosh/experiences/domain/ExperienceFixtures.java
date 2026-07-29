package com.ashutosh.experiences.domain;

import java.math.BigDecimal;

/**
 * Test fixture factory.
 *
 * Two things going on here:
 *  1. It lives in the com.ashutosh.experiences.domain package (under src/test)
 *     so it can reach Experience.setId, which is package-private. Hibernate
 *     assigns ids on persist, but a unit test that never touches a database
 *     still needs an entity with an id on it.
 *  2. It keeps the noise out of the tests. A test that starts with eight lines
 *     of constructor arguments buries the one line that actually matters.
 */
public final class ExperienceFixtures {

    private ExperienceFixtures() {
    }

    public static Experience withCapacity(Long id, int dailyCapacity) {
        Experience experience = new Experience(
                "Oxford Colleges Walking Tour",
                "Oxford",
                "Ninety minutes through Christ Church and the Bodleian.",
                new BigDecimal("25.00"),
                "GBP",
                90,
                dailyCapacity);
        experience.setId(id);
        return experience;
    }
}
