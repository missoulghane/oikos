package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface FinancialJournalEntryRepository {

    FinancialJournalEntry save(FinancialJournalEntry entry);

    Optional<FinancialJournalEntry> findById(FinancialJournalEntryId id);

    /**
     * financialAccountIds is resolved by the calling service from the property
     * (FinancialAccountRepository#findAllByPropertyId) - same join-in-the-
     * service pattern as ListInstallmentsByPropertyService, since a journal
     * entry only references its financial account, not the property directly.
     */
    Page<FinancialJournalEntry> findPageByFinancialAccountIds(List<FinancialAccountId> financialAccountIds,
                                                               FinancialJournalEntryFilter filter,
                                                               PageRequest pageRequest);
}
