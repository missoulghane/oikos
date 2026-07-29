package com.architek.oikos.user.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.valueobject.PartyInvitationTokenId;

/**
 * Opaque, single-use invitation token linking an owner's Party (created by a
 * manager/admin when attaching them to a unit) to an AppUser account: either
 * an existing one (matched by email) or a brand new one, chosen at
 * acceptance time. Keyed by partyId (not userId, since no AppUser may exist
 * yet) - deleteByPartyId lets re-inviting the same party supersede any
 * still-outstanding token (same idempotency pattern as
 * RequestPasswordResetService). fullName is denormalized from the Party at
 * issuance time, so accepting the invitation never needs a cross-feature
 * lookup back into party just to create a new account.
 */
public record PartyInvitationToken(PartyInvitationTokenId id, EntityId partyId, EmailVO email, String fullName,
                                    String token, Instant expiresAt) {

    public PartyInvitationToken {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(partyId, "partyId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
    }

    public static PartyInvitationToken issue(EntityId partyId, EmailVO email, String fullName, String token,
                                              Instant expiresAt) {
        return new PartyInvitationToken(PartyInvitationTokenId.newId(), partyId, email, fullName, token, expiresAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
