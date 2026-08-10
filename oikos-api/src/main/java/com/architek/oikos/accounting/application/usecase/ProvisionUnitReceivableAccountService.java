package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.port.in.ProvisionUnitReceivableAccountUseCase;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * ADR 0001 decision 5: numero 341150 + increment propre a la property - un
 * compte dedie par lot, jamais un compte collectif avec auxiliaire pour ce
 * role (a la difference de UNIT_ADVANCE).
 */
@Component
public class ProvisionUnitReceivableAccountService implements ProvisionUnitReceivableAccountUseCase {

    private static final String UNIT_RECEIVABLE_NUMBER_PREFIX = "341150";

    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerAccountNumberSequenceRepository sequenceRepository;

    public ProvisionUnitReceivableAccountService(LedgerAccountRepository ledgerAccountRepository,
                                                  LedgerAccountNumberSequenceRepository sequenceRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public LedgerAccountId provision(EntityId propertyId, EntityId unitId) {
        int increment = sequenceRepository.allocateNextIncrement(propertyId, UNIT_RECEIVABLE_NUMBER_PREFIX);
        AccountNumber accountNumber = AccountNumber.forSequence(UNIT_RECEIVABLE_NUMBER_PREFIX, increment);
        LedgerAccount account = LedgerAccount.create(LedgerAccountId.newId(), propertyId, unitId, accountNumber,
                "Coproprietaire - creance", 3, AccountNature.BALANCE_ASSET, false, AccountRole.UNIT_RECEIVABLE);
        return ledgerAccountRepository.save(account).getId();
    }
}
