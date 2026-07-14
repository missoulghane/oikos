package com.architek.oikos.shared.exception;

/**
 * Base type for all business-rule violations across every feature's domain
 * and application layers. Maps to HTTP 400 by default in the GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
