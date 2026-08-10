package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.port.in.ListJournalEntriesByTreasuryAccountUseCase;
import com.architek.oikos.accounting.application.query.ListJournalEntriesByTreasuryAccountQuery;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListJournalEntriesByTreasuryAccountService implements ListJournalEntriesByTreasuryAccountUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public ListJournalEntriesByTreasuryAccountService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JournalEntryView> list(ListJournalEntriesByTreasuryAccountQuery query) {
        return journalEntryRepository
                .findPageByTreasuryAccount(query.propertyId(), query.treasuryAccountId(), query.filter(),
                        query.pageRequest())
                .map(JournalEntryView::from);
    }
}
