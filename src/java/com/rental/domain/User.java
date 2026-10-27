package com.rental.domain;

import java.time.LocalDateTime;

/**
 * A person in the system: the item owner OR a renter (one table for both roles).
 *
 * <p>This is a Java "record": a compact immutable data class. The compiler generates the
 * constructor, getters (e.g. {@code user.username()}), equals, hashCode and toString.
 * Immutable = fields can never change after creation, which avoids a whole class of bugs.
 *
 * <p>We deliberately do NOT validate fields here. Validation belongs in the Service, and the
 * constraint tests must be able to build "bad" objects to prove the DATABASE rejects them.
 *
 * @param id        database id; {@code null} until the row has been saved
 * @param username  unique login/display name
 * @param password  empty (null) in project 1; used in project 2
 * @param createdAt set by the database; {@code null} until the row has been saved
 */
public record User(Long id, String username, String password, LocalDateTime createdAt) {

    /** Convenience factory for a brand-new, not-yet-saved user (no id, no timestamp). */
    public static User newUser(String username) {
        return new User(null, username, null, null);
    }
}