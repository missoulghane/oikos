package com.architek.oikos.accounting.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.AuxiliaryUnitBalance;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface JournalEntryRepository {

    JournalEntry save(JournalEntry entry);

    Optional<JournalEntry> findById(JournalEntryId id);

    /** I7: the next piece number to allocate for (property, exercise, journal), under a row lock. */
    int nextPieceNumber(EntityId propertyId, AccountingExerciseId exerciseId, JournalCode journalCode);

    /** Most recent piece date first. */
    Page<JournalEntry> findPageByPropertyId(EntityId propertyId, PageRequest pageRequest);

    /** Every entry attached to a period, regardless of status - used by P8's closing checks. */
    List<JournalEntry> findAllByPeriodId(PeriodId periodId);

    /** Operations of a single treasury account (accounting overview "click an account" flow),
     * most recent piece date first, narrowed by filter. */
    Page<JournalEntry> findPageByTreasuryAccount(EntityId propertyId, LedgerAccountId treasuryAccountId,
                                                  JournalEntryFilter filter, PageRequest pageRequest);

    /** Net balance (sum of CREDIT minus DEBIT, POSTED entries only) of a collective account for one
     * auxiliary unit - e.g. how much of the global "avances et acomptes recus" account belongs to
     * this lot (regularisation flow: how much advance is available to impute on its unpaid calls). */
    BigDecimal sumNetAmountForAuxiliaryUnit(EntityId propertyId, LedgerAccountId accountId, EntityId unitId);

    /** Same balance as sumNetAmountForAuxiliaryUnit, for every auxiliary unit that carries a
     * strictly positive one on this account - the property-wide regularisation sweep's starting
     * point (only units with something to reconcile are returned). */
    List<AuxiliaryUnitBalance> sumNetAmountGroupedByAuxiliaryUnit(EntityId propertyId, LedgerAccountId accountId);
}
