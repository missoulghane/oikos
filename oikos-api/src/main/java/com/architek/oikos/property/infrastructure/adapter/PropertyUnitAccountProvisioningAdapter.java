package com.architek.oikos.property.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.in.CreateUnitAccountUseCase;
import com.architek.oikos.property.application.port.out.UnitAccountProvisioningPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PropertyUnitAccountProvisioningAdapter implements UnitAccountProvisioningPort {

    private final CreateUnitAccountUseCase createUnitAccountUseCase;

    public PropertyUnitAccountProvisioningAdapter(CreateUnitAccountUseCase createUnitAccountUseCase) {
        this.createUnitAccountUseCase = createUnitAccountUseCase;
    }

    @Override
    public void provisionAccount(EntityId unitId, EntityId propertyId) {
        createUnitAccountUseCase.create(unitId, propertyId);
    }
}
