package com.ashutosh.experiences.service;

/**
 * Generates customer-facing booking references.
 *
 * This is an interface with a single implementation, which looks like
 * over-engineering until you try to unit test BookingService. If the service
 * called UUID.randomUUID() directly, the reference would be different on every
 * run and the test could not assert on it. Pulling the randomness behind an
 * interface means the test can inject a stub that always returns "VTR-TEST0001".
 *
 * The general principle: push non-determinism (randomness, clocks, network) to
 * the edges of your design so the logic in the middle stays testable.
 */
public interface ReferenceGenerator {

    String generate();
}
