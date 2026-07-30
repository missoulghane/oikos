package com.architek.oikos.accounting.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListUnitAccountMovementsQuery(EntityId unitId, PageRequest pageRequest) {
}
