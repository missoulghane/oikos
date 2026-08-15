package com.architek.oikos.installment.application.query;

import com.architek.oikos.installment.domain.valueobject.InstallmentFilter;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * search is not part of InstallmentFilter on purpose: it matches a lot number,
 * an owner's name or phone, none of which the installment repository can see.
 * It is resolved beforehand against the property module, and narrows the set of
 * units the listing runs on. null means no search.
 */
public record ListInstallmentsByPropertyQuery(EntityId propertyId, InstallmentFilter filter, PageRequest pageRequest,
                                                String search) {

    public ListInstallmentsByPropertyQuery(EntityId propertyId, InstallmentFilter filter, PageRequest pageRequest) {
        this(propertyId, filter, pageRequest, null);
    }
}
