package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record DeleteRecipientGroupCommand(RecipientGroupId groupId, EntityId propertyId) {
}
