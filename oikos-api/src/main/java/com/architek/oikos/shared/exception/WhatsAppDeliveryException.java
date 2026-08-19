package com.architek.oikos.shared.exception;

/**
 * Thrown by WhatsAppSenderPort adapters when the underlying provider call fails.
 * Not a BusinessException, same rationale as EmailDeliveryException: a rejected
 * or unreachable provider is a server-side integration failure, so it falls
 * through to GlobalExceptionHandler's generic handler (500), not the 400 mapping.
 */
public class WhatsAppDeliveryException extends RuntimeException {

    public WhatsAppDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
