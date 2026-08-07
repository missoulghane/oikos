package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Issued by invitation.infrastructure.adapter.InvitationBoardDirectoryAdapter
 * once a board invitation is accepted: the party already exists (resolved by
 * AcceptInvitationService) and userId is the accepting AppUser, so unlike
 * AddBoardMemberCommand there is no inline-party-creation shape to support.
 */
public record CreatePendingBoardMemberCommand(PropertyId propertyId, EntityId partyId, EntityId userId,
                                                 BoardRole boardRole) {
}
