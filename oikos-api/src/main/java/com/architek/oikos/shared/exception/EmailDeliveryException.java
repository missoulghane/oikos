package com.architek.oikos.shared.exception;

/**
 * Thrown by EmailSenderPort adapters when the underlying transport fails. Not a
 * BusinessException: this is a server-side integration failure (SMTP unreachable,
 * bad credentials, ...), not a client input error, so it falls through to
 * GlobalExceptionHandler's generic handler (500) rather than the 400 mapping.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
