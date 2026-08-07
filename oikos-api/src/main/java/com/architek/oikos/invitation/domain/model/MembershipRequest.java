package com.architek.oikos.invitation.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A single membership request against a PUBLIC invitation: one candidate, one chosen
 * unit. Multiple pending requests can target the same unit at once (the
 * unit stays selectable while pending, per product decision) - accepting one
 * auto-rejects the others on that unit (see AcceptMembershipRequestService).
 * Rejected/accepted rows are kept, never deleted. Immutable: every mutation
 * returns a new instance. Entity semantics: equals/hashCode are
 * identity-based.
 */
public final class MembershipRequest {

    private final MembershipRequestId id;
    private final EntityId invitationId;
    private final EntityId propertyId;
    private final EntityId unitId;
    private final EntityId partyId;
    private final EntityId userId;
    private final MembershipRequestStatus status;
    private final Instant decidedAt;
    private final EntityId decidedByUserId;
    private final String rejectionReason;

    private MembershipRequest(MembershipRequestId id, EntityId invitationId, EntityId propertyId, EntityId unitId,
                               EntityId partyId, EntityId userId, MembershipRequestStatus status, Instant decidedAt,
                               EntityId decidedByUserId, String rejectionReason) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.invitationId = Objects.requireNonNull(invitationId, "invitationId must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.partyId = Objects.requireNonNull(partyId, "partyId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.decidedAt = decidedAt;
        this.decidedByUserId = decidedByUserId;
        this.rejectionReason = rejectionReason;
    }

    public static MembershipRequest submit(MembershipRequestId id, EntityId invitationId, EntityId propertyId,
                                            EntityId unitId, EntityId partyId, EntityId userId) {
        return new MembershipRequest(id, invitationId, propertyId, unitId, partyId, userId,
                MembershipRequestStatus.PENDING, null, null, null);
    }

    public static MembershipRequest reconstruct(MembershipRequestId id, EntityId invitationId, EntityId propertyId,
                                                 EntityId unitId, EntityId partyId, EntityId userId,
                                                 MembershipRequestStatus status, Instant decidedAt,
                                                 EntityId decidedByUserId, String rejectionReason) {
        return new MembershipRequest(id, invitationId, propertyId, unitId, partyId, userId, status, decidedAt,
                decidedByUserId, rejectionReason);
    }

    public MembershipRequest accept(Instant decidedAt, EntityId decidedByUserId) {
        return new MembershipRequest(id, invitationId, propertyId, unitId, partyId, userId,
                MembershipRequestStatus.ACCEPTED, decidedAt, decidedByUserId, null);
    }

    public MembershipRequest reject(Instant decidedAt, EntityId decidedByUserId, String reason) {
        return new MembershipRequest(id, invitationId, propertyId, unitId, partyId, userId,
                MembershipRequestStatus.REJECTED, decidedAt, decidedByUserId, reason);
    }

    public boolean isPending() {
        return status == MembershipRequestStatus.PENDING;
    }

    public MembershipRequestId getId() {
        return id;
    }

    public EntityId getInvitationId() {
        return invitationId;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public EntityId getPartyId() {
        return partyId;
    }

    public EntityId getUserId() {
        return userId;
    }

    public MembershipRequestStatus getStatus() {
        return status;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public EntityId getDecidedByUserId() {
        return decidedByUserId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof MembershipRequest other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
