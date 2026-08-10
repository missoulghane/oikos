package com.architek.oikos.accounting.application.query;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GetUnitAdvanceBalanceQuery(EntityId propertyId, EntityId unitId) {
}
