package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.port.in.GetJournalEntryUseCase;
import com.architek.oikos.accounting.application.query.GetJournalEntryQuery;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotFoundException;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;

@Component
public class GetJournalEntryService implements GetJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public GetJournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public JournalEntryView get(GetJournalEntryQuery query) {
        return journalEntryRepository.findById(query.entryId())
                .map(JournalEntryView::from)
                .orElseThrow(() -> new JournalEntryNotFoundException(query.entryId()));
    }
}
