package com.architek.oikos.accounting.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to property's public port-in
 * (GetPropertyUseCase), never to property's repository directly (rule 4/6).
 * Named distinctly from installment's own InstallmentPropertyDirectoryAdapter
 * to avoid a Spring bean name collision between same-named classes in
 * different packages.
 */
@Component
public class AccountingPropertyDirectoryAdapter implements PropertyDirectoryPort {

    private final GetPropertyUseCase getPropertyUseCase;

    public AccountingPropertyDirectoryAdapter(GetPropertyUseCase getPropertyUseCase) {
        this.getPropertyUseCase = getPropertyUseCase;
    }

    @Override
    public boolean exists(EntityId propertyId) {
        try {
            getPropertyUseCase.getProperty(new GetPropertyQuery(new PropertyId(propertyId)));
            return true;
        } catch (PropertyNotFoundException e) {
            return false;
        }
    }
}
