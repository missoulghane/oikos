package com.architek.oikos.messaging.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A property's member as seen from messaging's point of view: a party
 * (owner-with-account or active board/manager seat), with a short French
 * display label for its role. hasLinkedAccount mirrors
 * property.application.dto.PropertyContactView/BoardMemberView's own field
 * of the same name - only members with hasLinkedAccount=true can actually
 * receive a message (see PartyAccountDirectoryPort.resolveUserIds).
 * unitNumbers lists every unit this party owns in the property (empty for a
 * board/manager seat that owns nothing there) - lets the recipient picker
 * disambiguate two people with the same name by their lot. isStaff is true
 * for a board/manager seat - lets the recipient picker offer "Le bureau" as
 * a one-click audience and filter candidates to it (see
 * StartBoardConversationService's own resolution of the same population).
 */
public record PropertyMemberInfo(EntityId partyId, String fullName, String roleLabel, boolean hasLinkedAccount,
                                  List<String> unitNumbers, boolean isStaff) {
}
