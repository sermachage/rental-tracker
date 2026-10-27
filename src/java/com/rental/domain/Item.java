package com.rental.domain;

import java.time.LocalDateTime;

/**
 * Something an owner lists for rent. Maps to a row of the {@code listed_items} table.
 *
 * <p>Immutable record: to "change" the status we create a modified COPY via
 * {@link #withStatus(ItemStatus)} and save that.
 *
 * @param itemId      database id; {@code null} until saved
 * @param ownerId     id of the {@link User} who listed it
 * @param itemName    short name, e.g. "Ladder"
 * @param description longer text, e.g. "6-step aluminium ladder"
 * @param costPerDay  price per day in whole currency units (simple int for this project)
 * @param status      current lifecycle state
 * @param createdAt   set by the database; {@code null} until saved
 */
public record Item(Long itemId, Long ownerId, String itemName, String description,
                   int costPerDay, ItemStatus status, LocalDateTime createdAt) {

    /** Factory for a new, unsaved listing. Every new listing starts as AVAILABLE. */
    public static Item newListing(Long ownerId, String itemName, String description, int costPerDay) {
        return new Item(null, ownerId, itemName, description, costPerDay, ItemStatus.AVAILABLE, null);
    }

    /** @return a copy of this item with a different status (original is untouched) */
    public Item withStatus(ItemStatus newStatus) {
        return new Item(itemId, ownerId, itemName, description, costPerDay, newStatus, createdAt);
    }
}