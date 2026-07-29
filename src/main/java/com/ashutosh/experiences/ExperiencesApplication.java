package com.ashutosh.experiences;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point.
 *
 * @SpringBootApplication is three annotations in one:
 *   - @Configuration       this class can define beans
 *   - @EnableAutoConfiguration  Spring inspects the classpath and configures
 *                          sensible defaults (it sees H2 and spring-data-jpa,
 *                          so it wires a DataSource and an EntityManager for us)
 *   - @ComponentScan       scan this package and everything below it for
 *                          @Component / @Service / @RestController beans
 *
 * That last one is why every other class lives under com.ashutosh.experiences.
 */
@SpringBootApplication
public class ExperiencesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExperiencesApplication.class, args);
    }
}
