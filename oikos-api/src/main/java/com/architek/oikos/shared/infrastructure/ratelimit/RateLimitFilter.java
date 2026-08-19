package com.architek.oikos.shared.infrastructure.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.architek.oikos.shared.domain.service.RateLimiter;
import com.architek.oikos.shared.web.ApiVersion;
import com.architek.oikos.shared.web.advice.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Caps how often one client may hit the endpoints that are open to the whole
 * internet: they are unauthenticated by necessity, and every one of them either
 * guesses at a credential or sends an email at the caller's command.
 *
 * <p>Runs before authentication, which is the whole point for /auth/login: a
 * password guess must be counted whether or not it turns out to be right.
 *
 * <p>Keyed on the client IP only. The quota that protects someone else's mailbox
 * is keyed on the target address instead, and lives in the application services
 * that know that address (RequestPasswordResetService and the two resend
 * services) - a filter would have to read and re-buffer the request body to see
 * it.
 *
 * <p>Behind the reverse proxy, that IP is the proxy's own unless Spring rewrites
 * it from X-Forwarded-For: the {@code docker} profile therefore sets
 * server.forward-headers-strategy=framework. It is set there and not in the base
 * configuration on purpose - trusting that header is only safe when the only way
 * in is through the proxy, which holds for the deployed stack (the API publishes
 * no port there) and not for an API run straight off a laptop.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String LOGIN = ApiVersion.V1 + "/auth/login";

    /** Endpoints dont chaque appel fait partir un email. */
    private static final List<String> EMAIL_SENDING = List.of(
            ApiVersion.V1 + "/auth/forgot-password",
            ApiVersion.V1 + "/users/resend-verification");

    /** Endpoints qui consomment un jeton reçu par email : autant de devinettes. */
    private static final List<String> TOKEN_ATTEMPT = List.of(
            ApiVersion.V1 + "/auth/reset-password",
            ApiVersion.V1 + "/users/verify",
            ApiVersion.V1 + "/users/activate-account",
            ApiVersion.V1 + "/users/accept-invitation");

    /** Endpoints qui créent des lignes en base sans qu'aucun compte n'existe encore. */
    private static final List<String> REGISTRATION = List.of(
            ApiVersion.V1 + "/users/register-property-user",
            ApiVersion.V1 + "/users/register-property-board-admin",
            ApiVersion.V1 + "/users/register-property-manager-admin",
            ApiVersion.V1 + "/users/onboarding-leads");

    private final RateLimiter rateLimiter;
    private final RateLimitPolicies policies;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiter rateLimiter, RateLimitPolicies policies, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.policies = policies;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Integer limit = limitFor(request);
        if (limit == null) {
            chain.doFilter(request, response);
            return;
        }
        String key = request.getRequestURI() + "|" + request.getRemoteAddr();
        RateLimiter.Decision decision = rateLimiter.tryConsume(key, limit, policies.window());
        if (decision.rejected()) {
            // Le chemin, pas le corps : l'adresse email visée n'a rien à faire
            // dans les journaux d'un endpoint anti-abus.
            log.warn("Rate limit reached on {} for {} - refused for {}s",
                    request.getRequestURI(), request.getRemoteAddr(), decision.retryAfter().toSeconds());
            writeTooManyRequests(request, response, decision.retryAfter());
            return;
        }
        chain.doFilter(request, response);
    }

    /** null : endpoint non plafonné, la requête passe sans être comptée. */
    private Integer limitFor(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (LOGIN.equals(path)) {
            return policies.loginPerIp();
        }
        if (EMAIL_SENDING.contains(path)) {
            return policies.emailSendPerIp();
        }
        if (TOKEN_ATTEMPT.contains(path)) {
            return policies.tokenAttemptPerIp();
        }
        if (REGISTRATION.contains(path)) {
            return policies.registrationPerIp();
        }
        return null;
    }

    /**
     * Même corps d'erreur que GlobalExceptionHandler, écrit avec le même
     * ObjectMapper que RestAuthenticationEntryPoint : un filtre écrit hors du
     * MVC, et deux formats d'erreur selon l'étage qui refuse obligeraient chaque
     * client à savoir lequel a parlé.
     */
    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response, Duration retryAfter)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, retryAfter.toSeconds())));
        ErrorResponse body = ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Trop de tentatives. Merci de réessayer dans quelques minutes.",
                request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
