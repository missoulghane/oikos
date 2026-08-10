package com.architek.oikos.messaging.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateMessageDraftCommand(EntityId propertyId, EntityId ownerId, SaveMessageDraftCommand payload) {
}
