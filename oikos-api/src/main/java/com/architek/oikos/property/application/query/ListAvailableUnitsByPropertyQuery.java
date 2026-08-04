package com.architek.oikos.property.application.query;

import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public record ListAvailableUnitsByPropertyQuery(PropertyId propertyId, PageRequest pageRequest) {
}
