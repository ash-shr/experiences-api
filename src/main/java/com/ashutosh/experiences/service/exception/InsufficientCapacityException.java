package com.ashutosh.experiences.service.exception;

/**
 * Thrown when a booking would push an experience past its capacity for that
 * date. Mapped to HTTP 409 Conflict -- the request was well formed, but it
 * conflicts with the current state of the resource. A 400 would be wrong here:
 * nothing about the request itself is invalid.
 */
public class InsufficientCapacityException extends RuntimeException {

    private final int requested;
    private final int remaining;

    public InsufficientCapacityException(int requested, int remaining) {
        super("Requested " + requested + " place(s) but only " + remaining + " remain");
        this.requested = requested;
        this.remaining = remaining;
    }

    public int getRequested() {
        return requested;
    }

    public int getRemaining() {
        return remaining;
    }
}
