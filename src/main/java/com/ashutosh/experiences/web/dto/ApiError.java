package com.ashutosh.experiences.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * One error shape for the whole API, so clients can parse failures the same way
 * every time instead of pattern-matching on prose.
 *
 * @JsonInclude(NON_NULL) keeps 'fieldErrors' out of the payload entirely when
 * it is null, rather than emitting "fieldErrors": null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message, null);
    }

    public static ApiError withFields(int status, String error, String message,
                                      Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, fieldErrors);
    }
}
