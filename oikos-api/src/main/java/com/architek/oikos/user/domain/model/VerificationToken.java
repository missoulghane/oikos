package com.architek.oikos.user.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.domain.valueobject.VerificationTokenId;

/**
 * Opaque account-activation token. Immutable; expiry is checked by isExpired(now).
 */
public record VerificationToken(VerificationTokenId id, UserId userId, String token, Instant expiresAt) {

    public VerificationToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
    }

    public static VerificationToken issue(UserId userId, String token, Instant expiresAt) {
        return new VerificationToken(VerificationTokenId.newId(), userId, token, expiresAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
