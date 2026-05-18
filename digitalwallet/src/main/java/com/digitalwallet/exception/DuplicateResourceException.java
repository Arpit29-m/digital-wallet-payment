package com.digitalwallet.exception;

/**
 * Thrown on unique constraint violations we can predict ahead of time
 * (email already registered, wallet number collision, etc.).
 * Keeps us from leaking raw DataIntegrityViolationExceptions to clients.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
