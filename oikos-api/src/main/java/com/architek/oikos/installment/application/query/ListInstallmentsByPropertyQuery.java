package com.architek.oikos.installment.application.query;

import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListInstallmentsByPropertyQuery(EntityId propertyId, InstallmentFilter filter, PageRequest pageRequest) {
}
