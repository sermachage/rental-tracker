package com.rental.domain;

/**
 * Thrown when code tries to move an item (or rental) to a status that the state machine forbids,
 * e.g. AVAILABLE -> AVAILABLE or UNLISTED -> RENTED.
 *
 * <p>It extends RuntimeException (an "unchecked" exception): breaking a transition rule is a
 * programming/business-rule error, so callers are not forced to write try/catch everywhere.
 * The Service layer decides when to catch it and show a friendly message.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    /**
     * @param from the current status name
     * @param to   the status we tried to move to
     */
    public InvalidStatusTransitionException(String from, String to) {
        super("Illegal status change: " + from + " -> " + to);
    }
}