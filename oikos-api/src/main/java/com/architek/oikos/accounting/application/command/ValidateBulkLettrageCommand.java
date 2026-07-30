package com.architek.oikos.accounting.application.command;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/** unitIds null or empty means: every unit of the property with something pending. */
public record ValidateBulkLettrageCommand(EntityId propertyId, List<EntityId> unitIds, EntityId validatedByUserId) {
}
