package com.ashutosh.experiences.service.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String reference) {
        super("No booking found with reference " + reference);
    }
}
