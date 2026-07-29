package com.ashutosh.experiences;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The cheapest useful test in any Spring project: does the application context
 * start at all? It catches missing beans, circular dependencies and broken
 * configuration before any other test has a chance to fail confusingly.
 */
@SpringBootTest
@DisplayName("Application context")
class ExperiencesApplicationTests {

    @Test
    @DisplayName("loads without errors")
    void contextLoads() {
    }
}
