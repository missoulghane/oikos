package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.AddBankAccountCommand;
import com.architek.oikos.accounting.application.port.in.AddBankAccountUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;

/** ADR 0001: numero 514100 + increment propre a la property. Une property peut avoir plusieurs comptes BANK (Partie 3). */
@Component
public class AddBankAccountService implements AddBankAccountUseCase {

    private static final String BANK_NUMBER_PREFIX = "514100";

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerAccountNumberSequenceRepository sequenceRepository;

    public AddBankAccountService(PropertyDirectoryPort propertyDirectoryPort,
                                  LedgerAccountRepository ledgerAccountRepository,
                                  LedgerAccountNumberSequenceRepository sequenceRepository) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public LedgerAccountId add(AddBankAccountCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }

        int increment = sequenceRepository.allocateNextIncrement(command.propertyId(), BANK_NUMBER_PREFIX);
        AccountNumber accountNumber = AccountNumber.forSequence(BANK_NUMBER_PREFIX, increment);
        LedgerAccount account = LedgerAccount.create(LedgerAccountId.newId(), command.propertyId(), null, accountNumber,
                command.label(), 5, AccountNature.BALANCE_ASSET, false, AccountRole.BANK)
                .withBankAccountNumber(command.bankAccountNumber());
        return ledgerAccountRepository.save(account).getId();
    }
}
