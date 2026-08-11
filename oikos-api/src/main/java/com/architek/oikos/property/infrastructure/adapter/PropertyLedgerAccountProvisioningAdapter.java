package com.architek.oikos.property.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.command.AddBankAccountCommand;
import com.architek.oikos.accounting.application.port.in.AddBankAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ProvisionPropertyCashAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ProvisionUnitReceivableAccountUseCase;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PropertyLedgerAccountProvisioningAdapter implements LedgerAccountProvisioningPort {

    private final ProvisionPropertyCashAccountUseCase provisionPropertyCashAccountUseCase;
    private final ProvisionUnitReceivableAccountUseCase provisionUnitReceivableAccountUseCase;
    private final AddBankAccountUseCase addBankAccountUseCase;

    public PropertyLedgerAccountProvisioningAdapter(ProvisionPropertyCashAccountUseCase provisionPropertyCashAccountUseCase,
                                                     ProvisionUnitReceivableAccountUseCase provisionUnitReceivableAccountUseCase,
                                                     AddBankAccountUseCase addBankAccountUseCase) {
        this.provisionPropertyCashAccountUseCase = provisionPropertyCashAccountUseCase;
        this.provisionUnitReceivableAccountUseCase = provisionUnitReceivableAccountUseCase;
        this.addBankAccountUseCase = addBankAccountUseCase;
    }

    @Override
    public void provisionPropertyCashAccount(EntityId propertyId) {
        provisionPropertyCashAccountUseCase.provision(propertyId);
    }

    @Override
    public void provisionUnitReceivableAccount(EntityId propertyId, EntityId unitId) {
        provisionUnitReceivableAccountUseCase.provision(propertyId, unitId);
    }

    @Override
    public void provisionBankAccount(EntityId propertyId, String label, String bankAccountNumber) {
        addBankAccountUseCase.add(new AddBankAccountCommand(propertyId, label, bankAccountNumber));
    }
}
