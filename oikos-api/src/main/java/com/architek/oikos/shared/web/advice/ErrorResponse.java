package com.architek.oikos.shared.web.advice;

import java.time.Instant;

/**
 * Standardized error body returned by the GlobalExceptionHandler.
 *
 * <p>{@code code} n'est renseigné que pour les erreurs que le client doit
 * distinguer les unes des autres (voir {@link com.architek.oikos.shared.exception.CodedException});
 * il vaut {@code null} partout ailleurs, où le statut HTTP suffit.
 */
public record ErrorResponse(int status, String error, String message, String code, String path, Instant timestamp) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, null, path);
    }

    public static ErrorResponse of(int status, String error, String message, String code, String path) {
        return new ErrorResponse(status, error, message, code, path, Instant.now());
    }
}
