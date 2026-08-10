package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.port.in.ListJournalEntriesByPropertyUseCase;
import com.architek.oikos.accounting.application.query.ListJournalEntriesByPropertyQuery;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListJournalEntriesByPropertyService implements ListJournalEntriesByPropertyUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public ListJournalEntriesByPropertyService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JournalEntryView> list(ListJournalEntriesByPropertyQuery query) {
        return journalEntryRepository.findPageByPropertyId(query.propertyId(), query.pageRequest())
                .map(JournalEntryView::from);
    }
}
