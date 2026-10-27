package com.rental.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Verifies the item state machine: every one of the 9 possible moves is checked. */
class ItemStatusTest {

    // The complete rule table, written independently of the production code.
    private static final Map<ItemStatus, Set<ItemStatus>> LEGAL = Map.of(
            ItemStatus.AVAILABLE, Set.of(ItemStatus.RENTED, ItemStatus.UNLISTED),
            ItemStatus.RENTED, Set.of(ItemStatus.AVAILABLE, ItemStatus.UNLISTED),
            ItemStatus.UNLISTED, Set.of(ItemStatus.AVAILABLE));

    @Test
    void everyPairOfStatesMatchesTheRuleTable() {
        for (ItemStatus from : ItemStatus.values()) {
            for (ItemStatus to : ItemStatus.values()) {
                boolean expected = LEGAL.get(from).contains(to);
                assertEquals(expected, from.canTransitionTo(to), from + " -> " + to);
            }
        }
    }

    @Test
    void transitionToReturnsTargetWhenLegal() {
        assertEquals(ItemStatus.RENTED, ItemStatus.AVAILABLE.transitionTo(ItemStatus.RENTED));
    }

    @Test
    void transitionToThrowsWhenIllegal() {
        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> ItemStatus.UNLISTED.transitionTo(ItemStatus.RENTED));
        assertTrue(ex.getMessage().contains("UNLISTED -> RENTED"));
    }

    @Test
    void dbValuesAreLowercaseWords() {
        assertEquals("available", ItemStatus.AVAILABLE.dbValue());
        assertEquals("rented", ItemStatus.RENTED.dbValue());
        assertEquals("unlisted", ItemStatus.UNLISTED.dbValue());
    }

    @Test
    void fromDbValueRoundTripsEveryStatus() {
        for (ItemStatus status : ItemStatus.values()) {
            assertEquals(status, ItemStatus.fromDbValue(status.dbValue()));
        }
    }

    @Test
    void fromDbValueRejectsUnknownAndNull() {
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.fromDbValue("broken"));
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.fromDbValue(null));
    }
}