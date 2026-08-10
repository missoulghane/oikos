package com.architek.oikos.property.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.in.ProvisionPropertyCashAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ProvisionUnitReceivableAccountUseCase;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PropertyLedgerAccountProvisioningAdapter implements LedgerAccountProvisioningPort {

    private final ProvisionPropertyCashAccountUseCase provisionPropertyCashAccountUseCase;
    private final ProvisionUnitReceivableAccountUseCase provisionUnitReceivableAccountUseCase;

    public PropertyLedgerAccountProvisioningAdapter(ProvisionPropertyCashAccountUseCase provisionPropertyCashAccountUseCase,
                                                     ProvisionUnitReceivableAccountUseCase provisionUnitReceivableAccountUseCase) {
        this.provisionPropertyCashAccountUseCase = provisionPropertyCashAccountUseCase;
        this.provisionUnitReceivableAccountUseCase = provisionUnitReceivableAccountUseCase;
    }

    @Override
    public void provisionPropertyCashAccount(EntityId propertyId) {
        provisionPropertyCashAccountUseCase.provision(propertyId);
    }

    @Override
    public void provisionUnitReceivableAccount(EntityId propertyId, EntityId unitId) {
        provisionUnitReceivableAccountUseCase.provision(propertyId, unitId);
    }
}
