package com.architek.oikos.shared.web.advice;

import java.time.Instant;

/**
 * Standardized error body returned by the GlobalExceptionHandler.
 */
public record ErrorResponse(int status, String error, String message, String path, Instant timestamp) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, Instant.now());
    }
}
