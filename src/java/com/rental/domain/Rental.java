package com.rental.domain;

import java.time.LocalDateTime;

/**
 * One loan of an item to a renter. Maps to a row of the {@code rentals} table.
 * Rentals are never deleted: together they are the history of who had what and when.
 *
 * @param rentalId   database id; {@code null} until saved
 * @param itemId     the rented item
 * @param renterId   the {@link User} renting it
 * @param startTime  when the rental begins
 * @param endTime    start time plus the rental duration (the due date)
 * @param returnedAt {@code null} until the item comes back
 * @param status     ACTIVE while out, CLOSED once returned
 */
public record Rental(Long rentalId, Long itemId, Long renterId, LocalDateTime startTime,
                     LocalDateTime endTime, LocalDateTime returnedAt, RentalStatus status) {

    /**
     * Factory for a new active rental. The end time is computed here from the duration, so
     * "end = start + days" is defined in exactly one place.
     */
    public static Rental newRental(Long itemId, Long renterId, LocalDateTime start, int days) {
        return new Rental(null, itemId, renterId, start, start.plusDays(days), null, RentalStatus.ACTIVE);
    }

    /** @return a copy marked CLOSED with the given return timestamp */
    public Rental closedAt(LocalDateTime returnTime) {
        return new Rental(rentalId, itemId, renterId, startTime, endTime, returnTime,
                status.transitionTo(RentalStatus.CLOSED));
    }
}