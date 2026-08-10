package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.UnitAdvanceView;
import com.architek.oikos.accounting.application.port.in.ListUnitsWithAvailableAdvanceUseCase;
import com.architek.oikos.accounting.application.query.ListUnitsWithAvailableAdvanceQuery;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;

@Component
public class ListUnitsWithAvailableAdvanceService implements ListUnitsWithAvailableAdvanceUseCase {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public ListUnitsWithAvailableAdvanceService(LedgerAccountRepository ledgerAccountRepository,
                                                 JournalEntryRepository journalEntryRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitAdvanceView> list(ListUnitsWithAvailableAdvanceQuery query) {
        LedgerAccount advanceAccount = ledgerAccountRepository.findGlobalByRole(AccountRole.UNIT_ADVANCE)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_ADVANCE, query.propertyId()));
        return journalEntryRepository.sumNetAmountGroupedByAuxiliaryUnit(query.propertyId(), advanceAccount.getId())
                .stream()
                .map(UnitAdvanceView::from)
                .toList();
    }
}
