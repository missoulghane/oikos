package com.architek.oikos.user.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve the membership requests submitted by a given
 * user, on behalf of the "my membership requests" self-service view. Keyed
 * by userId rather than partyId - unlike units/installments, a membership
 * request's party is only linked to the user's account once a manager
 * accepts it, so a partyId-based lookup would miss every still-PENDING
 * request. Implemented in user.infrastructure.adapter by delegating to
 * invitation's public port-in use cases - never to invitation's repository
 * directly (rule 6).
 */
public interface MembershipRequestDirectoryPort {

    List<OwnedMembershipRequestView> listMembershipRequestsForUser(EntityId userId);
}
