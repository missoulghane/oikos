package com.architek.oikos.document.infrastructure.storage;

/**
 * Unchecked wrapper for I/O failures against the active FileStoragePort
 * adapter. Not a BusinessException: a storage failure is an infrastructure
 * problem, not an invalid request, so it maps to the generic 500 handler
 * (same treatment as shared.exception.EmailDeliveryException).
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
