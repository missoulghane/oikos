package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.FinancialJournalEntryView;
import com.architek.oikos.accounting.application.port.in.ListFinancialJournalEntriesUseCase;
import com.architek.oikos.accounting.application.query.ListFinancialJournalEntriesQuery;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.shared.domain.pagination.Page;

/**
 * financialAccountIds is resolved from the property first (a
 * FinancialJournalEntry only references its financial account, not the
 * property directly) - same join-in-the-service pattern as
 * ListInstallmentsByPropertyService.
 */
@Component
public class ListFinancialJournalEntriesService implements ListFinancialJournalEntriesUseCase {

    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialJournalEntryRepository financialJournalEntryRepository;

    public ListFinancialJournalEntriesService(FinancialAccountRepository financialAccountRepository,
                                               FinancialJournalEntryRepository financialJournalEntryRepository) {
        this.financialAccountRepository = financialAccountRepository;
        this.financialJournalEntryRepository = financialJournalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialJournalEntryView> list(ListFinancialJournalEntriesQuery query) {
        List<FinancialAccountId> accountIds = financialAccountRepository.findAllByPropertyId(query.propertyId())
                .stream().map(FinancialAccount::getId).toList();
        return financialJournalEntryRepository.findPageByFinancialAccountIds(accountIds, query.filter(),
                query.pageRequest()).map(FinancialJournalEntryView::from);
    }
}
