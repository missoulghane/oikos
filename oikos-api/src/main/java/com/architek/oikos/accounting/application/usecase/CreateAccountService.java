package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateAccountCommand;
import com.architek.oikos.accounting.application.port.in.CreateAccountUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.application.port.out.UnitDirectoryPort;
import com.architek.oikos.accounting.domain.exception.DuplicateAccountException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.exception.UnitNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;

@Component
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final UnitDirectoryPort unitDirectoryPort;
    private final PropertyDirectoryPort propertyDirectoryPort;

    public CreateAccountService(AccountRepository accountRepository, UnitDirectoryPort unitDirectoryPort,
                                 PropertyDirectoryPort propertyDirectoryPort) {
        this.accountRepository = accountRepository;
        this.unitDirectoryPort = unitDirectoryPort;
        this.propertyDirectoryPort = propertyDirectoryPort;
    }

    @Override
    @Transactional
    public AccountId create(CreateAccountCommand command) {
        boolean holderExists = command.accountType() == AccountType.UNIT
                ? unitDirectoryPort.exists(command.holderId())
                : propertyDirectoryPort.exists(command.holderId());
        if (!holderExists) {
            throw command.accountType() == AccountType.UNIT
                    ? new UnitNotFoundException(command.holderId())
                    : new PropertyNotFoundException(command.holderId());
        }
        if (accountRepository.existsByHolderId(command.holderId(), command.accountType())) {
            throw new DuplicateAccountException(command.holderId(), command.accountType());
        }
        Account account = Account.create(AccountId.newId(), command.holderId(), command.accountType());
        return accountRepository.save(account).getId();
    }
}
