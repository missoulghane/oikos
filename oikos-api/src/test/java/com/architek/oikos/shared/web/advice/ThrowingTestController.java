package com.architek.oikos.shared.web.advice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.architek.oikos.shared.exception.ResourceNotFoundException;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Test-only controller used solely to exercise GlobalExceptionHandler's mapping
 * from each exception type to its HTTP status, in isolation of any real feature.
 */
@RestController
public class ThrowingTestController {

    @GetMapping("/test/not-found")
    void notFound() {
        throw new ResourceNotFoundException("nope");
    }

    @GetMapping("/test/unauthorized")
    void unauthorized() {
        throw new UnauthorizedException("nope");
    }

    @GetMapping("/test/conflict")
    void conflict() {
        throw new OptimisticLockingFailureException("nope");
    }

    @GetMapping("/test/data-integrity-violation")
    void dataIntegrityViolation() {
        throw new DataIntegrityViolationException("nope");
    }
}
