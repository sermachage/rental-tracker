package com.rental.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Verifies the rental state machine (only ACTIVE -> CLOSED is legal). */
class RentalStatusTest {

    @Test
    void onlyActiveToClosedIsLegal() {
        assertTrue(RentalStatus.ACTIVE.canTransitionTo(RentalStatus.CLOSED));
        assertFalse(RentalStatus.ACTIVE.canTransitionTo(RentalStatus.ACTIVE));
        assertFalse(RentalStatus.CLOSED.canTransitionTo(RentalStatus.ACTIVE));
        assertFalse(RentalStatus.CLOSED.canTransitionTo(RentalStatus.CLOSED));
    }

    @Test
    void transitionToReturnsTargetWhenLegal() {
        assertEquals(RentalStatus.CLOSED, RentalStatus.ACTIVE.transitionTo(RentalStatus.CLOSED));
    }

    @Test
    void transitionToThrowsWhenIllegal() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> RentalStatus.CLOSED.transitionTo(RentalStatus.ACTIVE));
    }

    @Test
    void dbValuesRoundTrip() {
        assertEquals("active", RentalStatus.ACTIVE.dbValue());
        assertEquals("closed", RentalStatus.CLOSED.dbValue());
        for (RentalStatus status : RentalStatus.values()) {
            assertEquals(status, RentalStatus.fromDbValue(status.dbValue()));
        }
    }

    @Test
    void fromDbValueRejectsUnknownAndNull() {
        assertThrows(IllegalArgumentException.class, () -> RentalStatus.fromDbValue("open"));
        assertThrows(IllegalArgumentException.class, () -> RentalStatus.fromDbValue(null));
    }
}