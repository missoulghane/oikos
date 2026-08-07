package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to attach an accepted board invitation's party to the
 * property's board. Implemented in invitation.infrastructure.adapter by
 * delegating to property's public port-in use cases - never to property's
 * repository directly (rule 6). Creates a PENDING_VALIDATION seat - the
 * PROPERTY_BOARD_MEMBER role is not granted yet, an admin must explicitly
 * validate the seat first (see property.application.usecase.ValidateBoardMemberService).
 */
public interface BoardDirectoryPort {

    void addPendingBoardMember(EntityId propertyId, EntityId partyId, EntityId userId, String boardRole);
}
