package com.architek.oikos.shared.exception;

/**
 * Base type for state-conflict errors across every feature (e.g. a resource
 * that just got claimed/modified by someone else) - distinct from
 * BusinessException (400, invalid input) since this is a genuine 409: the
 * request was valid when sent but the resource's state changed underneath
 * it. Maps to HTTP 409 by default in GlobalExceptionHandler.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
