package com.rental.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.rental.domain.*;
import com.rental.repository.*;

/** Enforces user, listing, rental, and return rules. */
public class RentalService {
    private final UserRepository users;
    private final ItemRepository items;
    private final RentalRepository rentals;

    public RentalService(UserRepository users, ItemRepository items, RentalRepository rentals) {
        this.users = users;
        this.items = items;
        this.rentals = rentals;
    }

    /** Loads the first account or creates it when the database is empty. */
    public User loadOwner(String username) {
        requireText(username, "Username");
        Optional<User> existing = users.findByUsername(username.trim());
        return existing.orElseGet(() -> users.insert(User.newUser(username.trim())));
    }

    /** Creates or reuses a renter for the supplied name. */
    public User renter(String username) {
        requireText(username, "Renter name");
        Optional<User> existing = users.findByUsername(username.trim());
        return existing.orElseGet(() -> users.insert(User.newUser(username.trim())));
    }

    /** Lists one owner. */
    public List<Item> inventory(long ownerId) {
        return items.findByOwner(ownerId);
    }

    /** Lists available items that can be rented. */
    public List<Item> availableInventory(long ownerId) {
        return items.findAvailableByOwner(ownerId);
    }

    /** Lists active rentals belonging to an owner, sorted by due date in the repository. */
    public List<Rental> activeRentals(long ownerId) {
        return rentals.findActiveByOwner(ownerId);
    }

    /** Finds an item and validates its owner. */
    public Item findOwnedItem(long ownerId, long itemId) {
        Item item = items.findById(itemId).orElseThrow(
                () -> new NotFoundException("Item not found: " + itemId));
        if (!item.ownerId().equals(ownerId)) {
            throw new NotFoundException("Item not found: " + itemId);
        }
        return item;
    }

    /** Finds a rental and validates that its item belongs to the owner. */
    public Rental findOwnedRental(long ownerId, long rentalId) {
        Rental rental = rentals.findById(rentalId).orElseThrow(
                () -> new NotFoundException("Rental not found: " + rentalId));
        Item item = findOwnedItem(ownerId, rental.itemId());
        if (item.status() == ItemStatus.AVAILABLE && rental.status() == RentalStatus.ACTIVE) {
            throw new NotFoundException("Rental not found: " + rentalId);
        }
        return rental;
    }

    /** Lists an item and starts its lifecycle. */
    public Item listItem(long ownerId, String name, String description, int costPerDay) {
        requireText(name, "Item name");
        requireText(description, "Description");
        if (costPerDay < 0) {
            throw new IllegalArgumentException("Cost per day cannot be negative");
        }
        return items.insert(Item.newListing(ownerId, name.trim(), description.trim(), costPerDay));
    }

    /** Delists an owned item. */
    public Item delist(long ownerId, long itemId) {
        Item item = findOwnedItem(ownerId, itemId);
        ItemStatus target = item.status() == ItemStatus.RENTED ? ItemStatus.UNLISTED : ItemStatus.UNLISTED;
        return items.updateStatus(item.withStatus(item.status().transitionTo(target)));
    }

    /** Relists an unlisted item, unless it still has an active rental. */
    public Item relist(long ownerId, long itemId) {
        Item item = findOwnedItem(ownerId, itemId);
        if (rentals.hasActiveRental(itemId)) {
            throw new IllegalStateException("Cannot relist an item while it is rented");
        }
        return items.updateStatus(item.withStatus(item.status().transitionTo(ItemStatus.AVAILABLE)));
    }

    /** Records a rental and transitions the item to RENTED. */
    public Rental rent(long ownerId, long itemId, String renterName, int days) {
        Item item = findOwnedItem(ownerId, itemId);
        if (item.status() != ItemStatus.AVAILABLE) {
            throw new IllegalStateException("Only available items can be rented");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Rental duration must be at least one day");
        }
        User renter = renter(renterName);
        LocalDateTime start = LocalDateTime.now().withNano(0);
        Rental rental = rentals.insert(Rental.newRental(itemId, renter.id(), start, days));
        items.updateStatus(item.withStatus(item.status().transitionTo(ItemStatus.RENTED)));
        return rental;
    }

    /** Closes a rental and returns the item to AVAILABLE or leaves it UNLISTED. */
    public Rental confirmReturn(long ownerId, long rentalId) {
        Rental rental = findOwnedRental(ownerId, rentalId);
        if (rental.status() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Rental is already closed");
        }
        Item item = findOwnedItem(ownerId, rental.itemId());
        Rental closed = rentals.update(rental.closedAt(LocalDateTime.now().withNano(0)));
        if (item.status() == ItemStatus.RENTED) {
            items.updateStatus(item.withStatus(item.status().transitionTo(ItemStatus.AVAILABLE)));
        }
        return closed;
    }

    private static void requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
    }
}
