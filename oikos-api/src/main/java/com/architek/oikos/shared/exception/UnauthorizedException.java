package com.architek.oikos.shared.exception;

/**
 * Thrown when an authenticated principal is not allowed to perform an action.
 * Maps to HTTP 403 in the GlobalExceptionHandler.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
