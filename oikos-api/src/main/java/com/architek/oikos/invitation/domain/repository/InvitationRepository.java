package com.architek.oikos.invitation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(InvitationId id);

    Optional<Invitation> findByToken(String token);

    Page<Invitation> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest);

    /**
     * Used by ListMembershipRequestsService to surface still-outstanding
     * PRIVATE invitations (issued, not yet accepted) in the manager's
     * unified membership-request overview - callers still need to filter
     * the result by Invitation.isUsable(now) themselves, since ACTIVE alone
     * doesn't account for expiry (there's no stored EXPIRED status).
     */
    List<Invitation> findAllByPropertyIdAndTypeAndStatus(EntityId propertyId, InvitationType type, InvitationStatus status);
}
