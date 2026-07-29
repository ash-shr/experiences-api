package com.ashutosh.experiences.web;

import com.ashutosh.experiences.service.exception.BookingNotFoundException;
import com.ashutosh.experiences.service.exception.ExperienceNotFoundException;
import com.ashutosh.experiences.service.exception.InsufficientCapacityException;
import com.ashutosh.experiences.web.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * One place that decides how exceptions become HTTP responses.
 *
 * The alternative is try/catch in every controller method, which duplicates the
 * mapping and guarantees the twentieth endpoint gets it slightly wrong.
 * @RestControllerAdvice registers these handlers across every controller.
 *
 * Getting status codes right is not pedantry. Clients, load balancers, retry
 * logic and monitoring all key off them: a 409 tells a caller "try a different
 * date", a 500 tells them "we are broken, page someone".
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ExperienceNotFoundException.class, BookingNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "Not Found", ex.getMessage()));
    }

    /**
     * 409 Conflict, not 400. The request was perfectly valid; it just cannot be
     * satisfied given the current state of the world.
     */
    @ExceptionHandler(InsufficientCapacityException.class)
    public ResponseEntity<ApiError> handleCapacity(InsufficientCapacityException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "Insufficient Capacity", ex.getMessage()));
    }

    /**
     * Thrown by Spring when @Valid fails. We unpack it into a field -> message
     * map so a front end can highlight the specific input that was wrong
     * instead of showing one generic error toast.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.withFields(400, "Validation Failed",
                        "One or more fields are invalid", fieldErrors));
    }
}
