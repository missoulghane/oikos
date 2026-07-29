package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.valueobject.UserId;

public record GrantCreatorAsManagerCommand(UserId userId, EntityId propertyId, PropertyRole role) {
}
