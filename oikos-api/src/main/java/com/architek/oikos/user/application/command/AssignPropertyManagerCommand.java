package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.PropertyRole;

public record AssignPropertyManagerCommand(EntityId propertyId, EmailVO email, PropertyRole role) {
}
