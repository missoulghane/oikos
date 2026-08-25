package com.architek.oikos.invitation.application.query;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record FindOutstandingPartyInvitationQuery(EntityId partyId) {
}
