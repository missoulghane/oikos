package com.architek.oikos.auth.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import com.architek.oikos.auth.domain.valueobject.RefreshTokenId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Immutable refresh-token aggregate. Only the SHA-256 hash of the raw opaque token is
 * kept (never the raw value). revoke() returns a new instance with the same id.
 * The generic {@link EntityId} keeps auth fully decoupled from the user feature: it
 * carries no dependency on user's own UserId type.
 */
public record RefreshToken(RefreshTokenId id, EntityId userId, String tokenHash, Set<String> authorities,
                            Instant expiresAt, boolean revoked) {

    public RefreshToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash must not be blank");
        }
        authorities = Set.copyOf(authorities);
    }

    public static RefreshToken issue(EntityId userId, String tokenHash, Set<String> authorities, Instant expiresAt) {
        return new RefreshToken(RefreshTokenId.newId(), userId, tokenHash, authorities, expiresAt, false);
    }

    public RefreshToken revoke() {
        return new RefreshToken(id, userId, tokenHash, authorities, expiresAt, true);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsable(Instant now) {
        return !revoked && !isExpired(now);
    }
}
