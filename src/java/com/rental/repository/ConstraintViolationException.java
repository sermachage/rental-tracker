package com.rental.repository;

/** Identifies a database constraint or foreign-key violation. */
public class ConstraintViolationException extends RuntimeException {
    public ConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
