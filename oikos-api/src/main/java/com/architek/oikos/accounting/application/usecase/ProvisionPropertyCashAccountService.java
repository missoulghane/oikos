package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.port.in.ProvisionPropertyCashAccountUseCase;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** ADR 0001 decision 6: numero 516100 + increment propre a la property. */
@Component
public class ProvisionPropertyCashAccountService implements ProvisionPropertyCashAccountUseCase {

    private static final String CASH_NUMBER_PREFIX = "516100";

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerAccountNumberSequenceRepository sequenceRepository;

    public ProvisionPropertyCashAccountService(LedgerAccountRepository ledgerAccountRepository,
                                                LedgerAccountNumberSequenceRepository sequenceRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public LedgerAccountId provision(EntityId propertyId) {
        int increment = sequenceRepository.allocateNextIncrement(propertyId, CASH_NUMBER_PREFIX);
        AccountNumber accountNumber = AccountNumber.forSequence(CASH_NUMBER_PREFIX, increment);
        LedgerAccount account = LedgerAccount.create(LedgerAccountId.newId(), propertyId, null, accountNumber,
                "Caisse", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH);
        return ledgerAccountRepository.save(account).getId();
    }
}
