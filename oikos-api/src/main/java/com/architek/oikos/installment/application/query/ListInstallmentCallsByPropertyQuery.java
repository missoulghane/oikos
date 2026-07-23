package com.architek.oikos.installment.application.query;

import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListInstallmentCallsByPropertyQuery(EntityId propertyId, PageRequest pageRequest) {
}
