package com.architek.oikos.invitation.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * propertyId is the generic {@link EntityId} rather than property's own
 * PropertyId, to keep invitation decoupled from property's identity types
 * (same rationale as PropertyRoleGrant.propertyId) - resolved back to
 * property-specific types only at the adapter boundary
 * (InvitationUnitDirectoryAdapter/InvitationPropertyDirectoryAdapter).
 * targetRole is the raw name of a user.domain.model.PropertyRole constant
 * (e.g. "PROPERTY_OWNER"), kept as a plain String here rather than
 * referencing that enum directly - unlike PropertyController, which
 * references it from the web layer, no cross-module domain-to-domain
 * reference exists elsewhere in this codebase, so this keeps invitation's
 * domain layer decoupled from user's; resolved back to PropertyRole only in
 * InvitationAccountDirectoryAdapter, right before calling into user's port.
 * Both remaining types (PUBLIC, PRIVATE) leave the unit choice to the
 * invitee - the unit is never fixed on the invitation itself, only recorded
 * on the resulting MembershipRequest/UnitOwnership once accepted.
 * consumedEmail records the email of whoever actually accepted a PRIVATE
 * invitation (null until consumed), so a manager can spot - after the fact,
 * non-blocking - a PRIVATE invitation accepted by someone other than
 * targetEmail; see Invitation.emailMismatch().
 * targetBoardRole is the raw name of a property.domain.valueobject.BoardRole
 * constant (e.g. "PRESIDENT"), kept as a plain String for the same
 * decoupling reason as targetRole - never referenced directly, resolved back
 * to BoardRole only in InvitationBoardDirectoryAdapter. Null for every
 * PROPERTY_OWNER invitation; set only when targetRole is
 * "PROPERTY_BOARD_MEMBER" (CreateBoardInvitationService).
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based.
 */
public final class Invitation {

    private final InvitationId id;
    private final EntityId propertyId;
    private final InvitationType type;
    private final String targetRole;
    private final EmailVO targetEmail;
    private final String token;
    private final InvitationStatus status;
    private final Instant expiresAt;
    private final EntityId createdByUserId;
    private final EmailVO consumedEmail;
    private final String targetBoardRole;

    private Invitation(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                        EmailVO targetEmail, String token, InvitationStatus status, Instant expiresAt,
                        EntityId createdByUserId, EmailVO consumedEmail, String targetBoardRole) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.targetRole = Objects.requireNonNull(targetRole, "targetRole must not be null");
        this.targetEmail = targetEmail;
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        this.token = token;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
        this.consumedEmail = consumedEmail;
        this.targetBoardRole = targetBoardRole;
    }

    public static Invitation issue(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                                    EmailVO targetEmail, String token, Instant expiresAt, EntityId createdByUserId,
                                    String targetBoardRole) {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.ACTIVE,
                expiresAt, createdByUserId, null, targetBoardRole);
    }

    public static Invitation reconstruct(InvitationId id, EntityId propertyId, InvitationType type,
                                          String targetRole, EmailVO targetEmail, String token,
                                          InvitationStatus status, Instant expiresAt, EntityId createdByUserId,
                                          EmailVO consumedEmail, String targetBoardRole) {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, status, expiresAt,
                createdByUserId, consumedEmail, targetBoardRole);
    }

    public Invitation disable() {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.DISABLED,
                expiresAt, createdByUserId, consumedEmail, targetBoardRole);
    }

    /** consumedEmail is the accepting caller's resolved account email - recorded even when it matches
     * targetEmail, so every PRIVATE invitation carries an audit trail of who actually consumed it. */
    public Invitation consume(EmailVO consumedEmail) {
        return new Invitation(id, propertyId, type, targetRole, targetEmail, token, InvitationStatus.CONSUMED,
                expiresAt, createdByUserId, consumedEmail, targetBoardRole);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isUsable(Instant now) {
        return status == InvitationStatus.ACTIVE && !isExpired(now);
    }

    /** True once a PRIVATE invitation has been accepted by an email different from the one it targeted -
     * never blocks acceptance (see AcceptInvitationService), only surfaced to managers for a later look. */
    public boolean emailMismatch() {
        return consumedEmail != null && targetEmail != null && !consumedEmail.equals(targetEmail);
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

    public EmailVO getConsumedEmail() {
        return consumedEmail;
    }

    public String getTargetBoardRole() {
        return targetBoardRole;
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
