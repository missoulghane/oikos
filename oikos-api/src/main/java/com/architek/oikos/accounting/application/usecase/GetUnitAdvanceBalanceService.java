package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.port.in.GetUnitAdvanceBalanceUseCase;
import com.architek.oikos.accounting.application.query.GetUnitAdvanceBalanceQuery;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;

@Component
public class GetUnitAdvanceBalanceService implements GetUnitAdvanceBalanceUseCase {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public GetUnitAdvanceBalanceService(LedgerAccountRepository ledgerAccountRepository,
                                         JournalEntryRepository journalEntryRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal get(GetUnitAdvanceBalanceQuery query) {
        LedgerAccount advanceAccount = ledgerAccountRepository.findGlobalByRole(AccountRole.UNIT_ADVANCE)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_ADVANCE, query.propertyId()));
        BigDecimal balance = journalEntryRepository.sumNetAmountForAuxiliaryUnit(query.propertyId(),
                advanceAccount.getId(), query.unitId());
        return balance.signum() > 0 ? balance : BigDecimal.ZERO;
    }
}
