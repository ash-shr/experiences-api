package com.ashutosh.experiences.service.exception;

/**
 * Thrown when a caller references an experience id that does not exist.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 *
 * RuntimeException rather than a checked exception: the caller cannot
 * meaningfully recover from this, so forcing every layer to declare it in a
 * throws clause adds noise without adding safety.
 */
public class ExperienceNotFoundException extends RuntimeException {

    public ExperienceNotFoundException(Long id) {
        super("No experience found with id " + id);
    }
}
