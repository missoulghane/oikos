package com.architek.oikos.accounting.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ValidateUnitLettrageCommand(EntityId unitId, EntityId validatedByUserId) {
}
