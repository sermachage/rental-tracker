package com.rental.repository;

/** Identifies an expected database row that could not be found. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
