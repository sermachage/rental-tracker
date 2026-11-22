package com.rental.repository;

/** Identifies an infrastructure failure while opening or using the database. */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
