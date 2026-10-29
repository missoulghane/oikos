package com.architek.oikos.accounting.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.AccountNetAmount;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
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

    /** Every entry attached to an exercise, regardless of status - used by the exercise closing checks. */
    List<JournalEntry> findAllByExerciseId(AccountingExerciseId exerciseId);

    /** Operations of a single treasury account (accounting overview "click an account" flow),
     * most recent piece date first, narrowed by filter. */
    Page<JournalEntry> findPageByTreasuryAccount(EntityId propertyId, LedgerAccountId treasuryAccountId,
                                                  JournalEntryFilter filter, PageRequest pageRequest);

    /** Net balance (sum of CREDIT minus DEBIT, POSTED and REVERSED entries) of a collective account for one
     * auxiliary unit - e.g. how much of the global "avances et acomptes recus" account belongs to
     * this lot (regularisation flow: how much advance is available to impute on its unpaid calls).
     * A REVERSED entry is cancelled by its posted mirror, not by being left out. */
    BigDecimal sumNetAmountForAuxiliaryUnit(EntityId propertyId, LedgerAccountId accountId, EntityId unitId);

    /**
     * Solde net (crédits moins débits, écritures comptabilisées) de chaque compte
     * mouvementé par une copropriété sur un exercice - ce que la clôture doit
     * solder, et que le solde du compte ne dit pas puisqu'il totalise toutes les
     * copropriétés pour les comptes partagés.
     */
    List<AccountNetAmount> sumNetAmountByAccountForExercise(EntityId propertyId, AccountingExerciseId exerciseId);
}
