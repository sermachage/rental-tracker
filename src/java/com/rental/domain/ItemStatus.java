package com.rental.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * The lifecycle state of a listed item, implemented as a STATE MACHINE.
 *
 * <p>An enum is a fixed set of named constants. Here each constant also carries the
 * knowledge of which states it may move to, so the rules live in ONE place:
 *
 * <pre>
 *   (new) -> AVAILABLE                 list item
 *   AVAILABLE -> RENTED                record rental
 *   RENTED    -> AVAILABLE             confirm return
 *   AVAILABLE -> UNLISTED              delist
 *   RENTED    -> UNLISTED              delist while out on a rental
 *   UNLISTED  -> AVAILABLE             relist (Service also checks there is no active rental)
 * </pre>
 *
 * <p>The database CHECK constraint is only a safety net; THIS class is the real gate.
 */
public enum ItemStatus {

    // Each constant is created with (databaseText, allowedNextStates).
    // Enum constants are built before their neighbours exist, so the allowed-targets are
    // filled in by the static block below instead of in the constructor arguments.
    AVAILABLE("available"),
    RENTED("rented"),
    UNLISTED("unlisted");

    /** The exact text stored in the database's status column. */
    private final String dbValue;

    /** The states this state is allowed to move to. Set in the static block. */
    private Set<ItemStatus> allowedTargets;

    ItemStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    // Static initialiser: runs once after all constants exist, so we can reference them.
    static {
        AVAILABLE.allowedTargets = EnumSet.of(RENTED, UNLISTED);
        RENTED.allowedTargets = EnumSet.of(AVAILABLE, UNLISTED);
        UNLISTED.allowedTargets = EnumSet.of(AVAILABLE);
    }

    /** @return the text to store in / compare with the database column */
    public String dbValue() {
        return dbValue;
    }

    /** @return true if moving from this state to {@code target} is legal */
    public boolean canTransitionTo(ItemStatus target) {
        return allowedTargets.contains(target);
    }

    /**
     * Returns {@code target} if the move is legal, otherwise throws.
     * The Service calls this before every status change.
     *
     * @throws InvalidStatusTransitionException if the move is not allowed
     */
    public ItemStatus transitionTo(ItemStatus target) {
        if (!canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(this.name(), target.name());
        }
        return target;
    }

    /**
     * Converts database text (e.g. "available") back into the enum.
     * The Repository wraps the IllegalArgumentException into our typed MappingException later.
     *
     * @throws IllegalArgumentException if the text matches no status (or is null)
     */
    public static ItemStatus fromDbValue(String text) {
        for (ItemStatus status : values()) {
            if (status.dbValue.equals(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown item status: " + text);
    }
}