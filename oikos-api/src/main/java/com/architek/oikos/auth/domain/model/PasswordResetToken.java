package com.architek.oikos.auth.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.auth.domain.valueobject.PasswordResetTokenId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Opaque, single-use password-reset token. Immutable; expiry is checked by
 * isExpired(now). Uses the generic {@link EntityId} rather than user's own UserId
 * type, keeping auth fully decoupled from the user feature (same rationale as
 * {@link RefreshToken}).
 *
 * <p>Only the token's SHA-256 hash is kept, never its value: the raw token lives
 * just long enough to go out in the email (see PasswordResetTokenGenerator).
 */
public record PasswordResetToken(PasswordResetTokenId id, EntityId userId, String tokenHash, Instant expiresAt) {

    public PasswordResetToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }
    }

    public static PasswordResetToken issue(EntityId userId, String tokenHash, Instant expiresAt) {
        return new PasswordResetToken(PasswordResetTokenId.newId(), userId, tokenHash, expiresAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
