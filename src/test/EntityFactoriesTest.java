package com.rental.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Tests the small helper methods on the User, Item and Rental records. */
class EntityFactoriesTest {

    @Test
    void newUserHasNoIdPasswordOrTimestamp() {
        User user = User.newUser("liisa");
        assertEquals("liisa", user.username());
        assertNull(user.id());
        assertNull(user.password());
        assertNull(user.createdAt());
    }

    @Test
    void newListingStartsAvailableAndUnsaved() {
        Item item = Item.newListing(1L, "Ladder", "6-step", 5);
        assertEquals(ItemStatus.AVAILABLE, item.status());
        assertNull(item.itemId());
        assertEquals(5, item.costPerDay());
    }

    @Test
    void withStatusReturnsCopyAndLeavesOriginalUntouched() {
        Item original = Item.newListing(1L, "Ladder", "6-step", 5);
        Item changed = original.withStatus(ItemStatus.RENTED);
        assertEquals(ItemStatus.RENTED, changed.status());
        assertEquals(ItemStatus.AVAILABLE, original.status()); // immutability
        assertEquals(original.itemName(), changed.itemName());
    }

    @Test
    void newRentalEndsStartPlusDays() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 16, 14, 30);
        Rental rental = Rental.newRental(1L, 2L, start, 3);
        assertEquals(LocalDateTime.of(2026, 6, 19, 14, 30), rental.endTime());
        assertEquals(RentalStatus.ACTIVE, rental.status());
        assertNull(rental.returnedAt());
    }

    @Test
    void closedAtMarksClosedAndStoresReturnTime() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 16, 14, 30);
        LocalDateTime back = LocalDateTime.of(2026, 6, 18, 9, 0);
        Rental closed = Rental.newRental(1L, 2L, start, 3).closedAt(back);
        assertEquals(RentalStatus.CLOSED, closed.status());
        assertEquals(back, closed.returnedAt());
    }

    @Test
    void cannotCloseAnAlreadyClosedRental() {
        LocalDateTime t = LocalDateTime.of(2026, 6, 16, 14, 30);
        Rental closed = Rental.newRental(1L, 2L, t, 1).closedAt(t);
        assertThrows(InvalidStatusTransitionException.class, () -> closed.closedAt(t));
    }
}