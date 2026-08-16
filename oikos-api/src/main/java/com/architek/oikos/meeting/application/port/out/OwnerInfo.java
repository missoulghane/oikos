package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** email may be null: a party can be recorded with a phone number alone. */
public record OwnerInfo(EntityId partyId, String fullName, String email) {
}
