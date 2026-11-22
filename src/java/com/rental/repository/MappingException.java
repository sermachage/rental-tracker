package com.rental.repository;

/** Identifies a database row that cannot be mapped safely into a domain object. */
public class MappingException extends RuntimeException {
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
