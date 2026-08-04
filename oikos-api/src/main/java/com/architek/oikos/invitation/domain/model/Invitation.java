package com.architek.oikos.invitation.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * propertyId/unitId are the generic {@link EntityId} rather than property's
 * own PropertyId/UnitId, to keep invitation decoupled from property's
 * identity types (same rationale as PropertyRoleGrant.propertyId) - resolved
 * back to property-specific types only at the adapter boundary
 * (InvitationUnitDirectoryAdapter/InvitationPropertyDirectoryAdapter).
 * targetRole is the raw name of a user.domain.model.PropertyRole constant
 * (e.g. "PROPERTY_OWNER"), kept as a plain String here rather than
 * referencing that enum directly - unlike PropertyController, which
 * references it from the web layer, no cross-module domain-to-domain
 * reference exists elsewhere in this codebase, so this keeps invitation's
 * domain layer decoupled from user's; resolved back to PropertyRole only in
 * InvitationAccountDirectoryAdapter, right before calling into user's port.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class Invitation {

    private final InvitationId id;
    private final EntityId propertyId;
    private final InvitationType type;
    private final String targetRole;
    private final EntityId unitId;
    private final EmailVO targetEmail;
    private final String token;
    private final InvitationStatus status;
    private final Instant expiresAt;
    private final EntityId createdByUserId;

    private Invitation(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                        EntityId unitId, EmailVO targetEmail, String token, InvitationStatus status,
                        Instant expiresAt, EntityId createdByUserId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.targetRole = Objects.requireNonNull(targetRole, "targetRole must not be null");
        this.unitId = unitId;
        this.targetEmail = targetEmail;
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        this.token = token;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
    }

    public static Invitation issue(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                                    EntityId unitId, EmailVO targetEmail, String token, Instant expiresAt,
                                    EntityId createdByUserId) {
        return new Invitation(id, propertyId, type, targetRole, unitId, targetEmail, token,
                InvitationStatus.ACTIVE, expiresAt, createdByUserId);
    }

    public static Invitation reconstruct(InvitationId id, EntityId propertyId, InvitationType type,
                                          String targetRole, EntityId unitId, EmailVO targetEmail, String token,
                                          InvitationStatus status, Instant expiresAt, EntityId createdByUserId) {
        return new Invitation(id, propertyId, type, targetRole, unitId, targetEmail, token, status, expiresAt,
                createdByUserId);
    }

    public Invitation disable() {
        return new Invitation(id, propertyId, type, targetRole, unitId, targetEmail, token, InvitationStatus.DISABLED,
                expiresAt, createdByUserId);
    }

    public Invitation consume() {
        return new Invitation(id, propertyId, type, targetRole, unitId, targetEmail, token, InvitationStatus.CONSUMED,
                expiresAt, createdByUserId);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsable(Instant now) {
        return status == InvitationStatus.ACTIVE && !isExpired(now);
    }

    public InvitationId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public InvitationType getType() {
        return type;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public EmailVO getTargetEmail() {
        return targetEmail;
    }

    public String getToken() {
        return token;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public EntityId getCreatedByUserId() {
        return createdByUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Invitation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
