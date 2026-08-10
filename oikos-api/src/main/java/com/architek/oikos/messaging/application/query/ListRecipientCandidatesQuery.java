package com.architek.oikos.messaging.application.query;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListRecipientCandidatesQuery(EntityId propertyId, EntityId userId, String search) {
}
