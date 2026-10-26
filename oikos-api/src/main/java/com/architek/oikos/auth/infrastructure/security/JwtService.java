package com.architek.oikos.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.architek.oikos.auth.application.port.out.JwtTokenPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * JWT HMAC HS256 access-token issuance and parsing (io.jsonwebtoken / JJWT). Only
 * generateAccessToken/accessTokenTtlSeconds are exposed to the application layer
 * (via JwtTokenPort); parse() is used solely by JwtAuthenticationFilter, both of
 * which live in this infrastructure/security package.
 */
@Component
public class JwtService implements JwtTokenPort {

    private static final String AUTHORITIES_CLAIM = "authorities";

    /**
     * Authority prefix carried by an onboarding token, suffixed with the property
     * it may configure. Such a token deliberately holds no ROLE_*: it is the only
     * marker PropertyAccessEvaluator needs to deny every other rule (see
     * canConfigureOnboarding), so an account that has not verified its email yet
     * cannot use it as a general-purpose session.
     */
    public static final String ONBOARDING_AUTHORITY_PREFIX = "ONBOARDING_";

    /**
     * Authority of the token issued at step 1 of the wizard, before the account
     * has a property. Shares the onboarding prefix on purpose, so every rule but
     * canCreateBoardAdminProperty denies it the same way; cannot collide with a
     * property-scoped one, whose suffix is a UUID.
     */
    public static final String ACCOUNT_ONBOARDING_AUTHORITY = ONBOARDING_AUTHORITY_PREFIX + "ACCOUNT";

    private final SecretKey signingKey;
    private final long accessTokenTtlSeconds;
    private final long onboardingTokenTtlSeconds;

    public JwtService(@Value("${oikos.security.jwt.secret}") String secret,
                       @Value("${oikos.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds,
                       @Value("${oikos.security.jwt.onboarding-token-ttl-seconds}") long onboardingTokenTtlSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.onboardingTokenTtlSeconds = onboardingTokenTtlSeconds;
    }

    @Override
    public String generateAccessToken(EntityId userId, Set<String> authorities) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(AUTHORITIES_CLAIM, authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Short-lived token handed to the volunteer-syndic wizard when its account is
     * created (end of step 1), so step 2 can create the account's property
     * without the account being verified. Scoped to that single endpoint; the
     * property-scoped token below takes over once the property exists.
     */
    @Override
    public String generateAccountOnboardingToken(EntityId userId) {
        return signOnboardingToken(userId, ACCOUNT_ONBOARDING_AUTHORITY);
    }

    /**
     * Short-lived token handed to the volunteer-syndic wizard when its property is
     * created (end of step 2), so steps 3 to 7 can post their configuration
     * without the account being verified - and without login being weakened.
     * Scoped to one property and one endpoint; when it expires the visitor simply
     * verifies their email and finishes the wizard as an ordinary board admin.
     */
    @Override
    public String generateOnboardingToken(EntityId userId, EntityId propertyId) {
        return signOnboardingToken(userId, ONBOARDING_AUTHORITY_PREFIX + propertyId);
    }

    private String signOnboardingToken(EntityId userId, String authority) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(AUTHORITIES_CLAIM, Set.of(authority))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(onboardingTokenTtlSeconds)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public long accessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    @Override
    public long onboardingTokenTtlSeconds() {
        return onboardingTokenTtlSeconds;
    }

    public Optional<Jws<Claims>> parse(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static Set<String> authoritiesOf(Claims claims) {
        List<String> raw = claims.get(AUTHORITIES_CLAIM, List.class);
        return raw == null ? Set.of() : raw.stream().collect(Collectors.toUnmodifiableSet());
    }
}
