package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AssignPropertyManagerCommand(EntityId propertyId, EmailVO email) {
}
