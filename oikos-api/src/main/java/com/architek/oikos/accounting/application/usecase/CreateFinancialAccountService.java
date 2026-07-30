package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateFinancialAccountCommand;
import com.architek.oikos.accounting.application.port.in.CreateFinancialAccountUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;

/** Spec &sect;4: a property can hold several financial accounts (caisse/banques). */
@Component
public class CreateFinancialAccountService implements CreateFinancialAccountUseCase {

    private final FinancialAccountRepository financialAccountRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;

    public CreateFinancialAccountService(FinancialAccountRepository financialAccountRepository,
                                          PropertyDirectoryPort propertyDirectoryPort) {
        this.financialAccountRepository = financialAccountRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
    }

    @Override
    @Transactional
    public FinancialAccountId create(CreateFinancialAccountCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }
        FinancialAccount account = FinancialAccount.create(FinancialAccountId.newId(), command.propertyId(),
                command.name(), command.type(), command.currency());
        return financialAccountRepository.save(account).getId();
    }
}
