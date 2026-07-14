package com.architek.oikos.shared.exception;

/**
 * Thrown when a lookup by identifier fails. Maps to HTTP 404 in the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forResource(String resourceName, Object identifier) {
        return new ResourceNotFoundException(resourceName + " not found with id: " + identifier);
    }
}
