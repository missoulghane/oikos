package com.architek.oikos.messaging.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A property's member as seen from messaging's point of view: a party
 * (owner-with-account or active board/manager seat), with a short French
 * display label for its role. hasLinkedAccount mirrors
 * property.application.dto.PropertyContactView/BoardMemberView's own field
 * of the same name - only members with hasLinkedAccount=true can actually
 * receive a message (see PartyAccountDirectoryPort.resolveUserIds).
 */
public record PropertyMemberInfo(EntityId partyId, String fullName, String roleLabel, boolean hasLinkedAccount) {
}
