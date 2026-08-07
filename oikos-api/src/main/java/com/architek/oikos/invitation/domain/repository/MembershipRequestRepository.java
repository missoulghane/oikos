package com.architek.oikos.invitation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface MembershipRequestRepository {

    MembershipRequest save(MembershipRequest membershipRequest);

    Optional<MembershipRequest> findById(MembershipRequestId id);

    Page<MembershipRequest> findAllByPropertyId(EntityId propertyId, PageRequest pageRequest);

    /**
     * Unpaged variant, used by ListMembershipRequestsService to merge real
     * requests with still-outstanding PRIVATE invitations (see
     * InvitationRepository.findAllByPropertyIdAndTypeAndStatus) into one
     * unified, in-memory-paginated overview - two independently-paged JPA
     * queries can't be merged into a single consistent page.
     */
    List<MembershipRequest> findAllByPropertyId(EntityId propertyId);

    /**
     * Every still-PENDING request targeting this unit, used to auto-reject
     * the siblings of whichever one a manager just accepted.
     */
    List<MembershipRequest> findAllPendingByUnitId(EntityId unitId);

    /**
     * Every request (any status) submitted by this user - the "my membership
     * requests" self-service view. Keyed by userId rather than partyId:
     * the party is only linked to the user's account once a manager accepts
     * the request (see GrantPropertyRoleService), so a partyId-based lookup
     * would miss every still-PENDING request - exactly the ones a requester
     * most wants to see the status of.
     */
    List<MembershipRequest> findAllByUserId(EntityId userId);

    /**
     * Used by SubmitMembershipRequestService to make submission idempotent:
     * the same invitation link can legitimately be posted twice for the same
     * user+unit (page reload, a registration-time submission followed by the
     * post-login auto-confirm retry, double-click) - returning the existing
     * row instead of creating a duplicate PENDING request.
     */
    Optional<MembershipRequest> findByInvitationIdAndUnitIdAndUserId(EntityId invitationId, EntityId unitId, EntityId userId);
}
