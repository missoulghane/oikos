package com.architek.oikos.property.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.command.CreateAccountCommand;
import com.architek.oikos.accounting.application.port.in.CreateAccountUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.property.application.port.out.PropertyAccountProvisioningPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to accounting's public port-in use case
 * (CreateAccountUseCase), never to accounting's repository directly (rule 6).
 * Named distinctly from accounting's own AccountingPropertyDirectoryAdapter and
 * installment's own InstallmentPropertyDirectoryAdapter to avoid a Spring bean
 * name collision between same-named classes in different packages.
 */
@Component
public class PropertyAccountProvisioningAdapter implements PropertyAccountProvisioningPort {

    private final CreateAccountUseCase createAccountUseCase;

    public PropertyAccountProvisioningAdapter(CreateAccountUseCase createAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
    }

    @Override
    public void provisionAccount(EntityId propertyId) {
        createAccountUseCase.create(new CreateAccountCommand(propertyId, AccountType.PROPERTY));
    }
}
