package com.architek.oikos.shared.exception;

import java.time.Duration;

/**
 * Raised when a caller has used up an anti-abuse quota. Maps to HTTP 429 in the
 * GlobalExceptionHandler, which turns {@link #retryAfter()} into the Retry-After
 * header.
 *
 * <p>Not a {@link BusinessException}: nothing about the request is wrong, it just
 * came too soon, and a 400 would tell the caller to fix something that is fine.
 */
public class TooManyRequestsException extends RuntimeException {

    private final transient Duration retryAfter;

    public TooManyRequestsException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
