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

    private final SecretKey signingKey;
    private final long accessTokenTtlSeconds;

    public JwtService(@Value("${oikos.security.jwt.secret}") String secret,
                       @Value("${oikos.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
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

    @Override
    public long accessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
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
