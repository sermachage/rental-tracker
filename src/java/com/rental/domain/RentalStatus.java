package com.rental.domain;

/**
 * Lifecycle of a rental record: ACTIVE while the item is out, CLOSED once returned.
 * The only legal move is ACTIVE -> CLOSED (a rental is never reopened or deleted).
 */
public enum RentalStatus {
    ACTIVE("active"),
    CLOSED("closed");

    /** The exact text stored in the database's status column. */
    private final String dbValue;

    RentalStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    /** @return the text to store in the database */
    public String dbValue() {
        return dbValue;
    }

    /** @return true only for ACTIVE -> CLOSED */
    public boolean canTransitionTo(RentalStatus target) {
        return this == ACTIVE && target == CLOSED;
    }

    /**
     * Returns {@code target} if the move is legal, otherwise throws.
     *
     * @throws InvalidStatusTransitionException if the move is not allowed
     */
    public RentalStatus transitionTo(RentalStatus target) {
        if (!canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(this.name(), target.name());
        }
        return target;
    }

    /**
     * Converts database text back into the enum.
     *
     * @throws IllegalArgumentException if the text matches no status (or is null)
     */
    public static RentalStatus fromDbValue(String text) {
        for (RentalStatus status : values()) {
            if (status.dbValue.equals(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown rental status: " + text);
    }
}