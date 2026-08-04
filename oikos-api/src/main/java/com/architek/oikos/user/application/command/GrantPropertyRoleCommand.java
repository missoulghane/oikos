package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.valueobject.UserId;

public record GrantPropertyRoleCommand(UserId userId, EntityId partyId, EntityId propertyId, PropertyRole role) {
}
