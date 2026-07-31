package com.ashutosh.experiences.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * A bookable travel experience.
 *
 * Design notes:
 *  - price is BigDecimal, never double. Binary floating point cannot represent
 *    0.10 exactly, so money arithmetic in double silently drifts. BigDecimal is
 *    the correct type for currency in Java.
 *  - dailyCapacity is how many people can be booked onto this experience on a
 *    single date. It is the constraint the booking logic enforces.
 *  - There is no @OneToMany back to Booking. The relationship is only navigable
 *    from Booking -> Experience. Bidirectional mappings are easy to get wrong
 *    and we do not need it: when we want bookings for an experience we query
 *    the BookingRepository directly.
 */
@Entity
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String city;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int dailyCapacity;

    @Column(nullable = false)
    private boolean familyFriendly;

    /**
     * JPA requires a no-arg constructor so Hibernate can instantiate the entity
     * via reflection before populating its fields. It is protected rather than
     * public to discourage application code from using it.
     */
    protected Experience() {
    }

    public Experience(String title, String city, String description, BigDecimal price,
                      String currency, int durationMinutes, int dailyCapacity) {
        this.title = title;
        this.city = city;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.durationMinutes = durationMinutes;
        this.dailyCapacity = dailyCapacity;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCity() {
        return city;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getDailyCapacity() {
        return dailyCapacity;
    }

    public boolean isFamilyFriendly() {
        return familyFriendly;
    }

    /**
     * Test-only hook. Hibernate assigns the id on persist, but a unit test that
     * never touches a database still needs an entity with an id on it.
     */
    void setId(Long id) {
        this.id = id;
    }
}
