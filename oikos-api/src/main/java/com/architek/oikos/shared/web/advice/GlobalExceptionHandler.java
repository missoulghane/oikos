package com.architek.oikos.shared.web.advice;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ResourceNotFoundException;
import com.architek.oikos.shared.exception.TooManyRequestsException;
import com.architek.oikos.shared.exception.UnauthorizedException;
import com.architek.oikos.shared.exception.WhatsAppDeliveryException;

/**
 * Central mapping from exceptions to standardized HTTP error responses,
 * shared by every feature's web layer.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), codeOf(ex), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), codeOf(ex), request);
    }

    /**
     * 429 plutôt que 400 : la requête est valide, elle arrive seulement trop tôt.
     * Retry-After dit combien de temps patienter - sans lui, un client qui
     * réessaie en boucle est le comportement le plus naturel du monde.
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, ex.retryAfter().toSeconds())))
                .body(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(),
                        HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), codeOf(ex), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The resource was modified concurrently, please retry", request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), codeOf(ex), request);
    }

    /**
     * Triggered by a database FK constraint set to ON DELETE RESTRICT.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT,
                "This resource cannot be deleted or modified because it is still referenced by other data.", request);
    }

    /**
     * 502 plutôt que 500 : l'échec vient du fournisseur, pas de nous, et son motif
     * est renvoyé tel quel. C'est presque toujours un point de configuration
     * (expéditeur, fenêtre de 24 h, numéro non inscrit) que l'appelant peut
     * corriger - le masquer derrière « une erreur inattendue » obligeait à ouvrir
     * les logs du serveur pour l'apprendre.
     */
    @ExceptionHandler(WhatsAppDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleWhatsAppDelivery(WhatsAppDeliveryException ex, HttpServletRequest request) {
        log.error("WhatsApp delivery failed", ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Access is denied", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        return build(status, message, null, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String code, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, code,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Les interfaces n'affichent jamais {@code message} - il est technique et en
     * anglais. Quand une exception porte un code, il voyage avec la réponse : c'est
     * le seul moyen pour un client de distinguer deux erreurs de même statut sans
     * se mettre à comparer des messages.
     */
    private static String codeOf(Throwable ex) {
        return ex instanceof CodedException coded ? coded.errorCode() : null;
    }
}
