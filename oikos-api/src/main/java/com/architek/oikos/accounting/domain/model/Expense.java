package com.architek.oikos.accounting.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A supplier expense (P4/P5, spec &sect;6): always created together with
 * the AC/BQ/CA journal entry it triggers (debit the caller-chosen charge
 * account, credit the caller-chosen treasury account - see
 * RecordSupplierPaymentService), same "created together with its journal
 * entry" principle as the pre-PCM Expense. No supplier identity is tracked
 * (Partie 2): a supplier is just a charge account/label, not a party.
 * ledgerAccountId/journalEntryId use their own strongly-typed ids since
 * Expense lives in the same module as both.
 * Immutable: entity semantics, equals/hashCode are identity-based (on id).
 */
public final class Expense {

    private final ExpenseId id;
    private final EntityId propertyId;
    private final LocalDate date;
    private final LedgerAccountId ledgerAccountId;
    private final Amount amount;
    private final String description;
    private final String receiptReference;
    private final JournalEntryId journalEntryId;

    private Expense(ExpenseId id, EntityId propertyId, LocalDate date, LedgerAccountId ledgerAccountId,
                     Amount amount, String description, String receiptReference, JournalEntryId journalEntryId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.ledgerAccountId = Objects.requireNonNull(ledgerAccountId, "ledgerAccountId must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.description = description;
        this.receiptReference = receiptReference;
        this.journalEntryId = Objects.requireNonNull(journalEntryId, "journalEntryId must not be null");
    }

    public static Expense create(ExpenseId id, EntityId propertyId, LocalDate date, LedgerAccountId ledgerAccountId,
                                  Amount amount, String description, String receiptReference,
                                  JournalEntryId journalEntryId) {
        return new Expense(id, propertyId, date, ledgerAccountId, amount, description, receiptReference,
                journalEntryId);
    }

    public static Expense reconstruct(ExpenseId id, EntityId propertyId, LocalDate date,
                                       LedgerAccountId ledgerAccountId, Amount amount, String description,
                                       String receiptReference, JournalEntryId journalEntryId) {
        return new Expense(id, propertyId, date, ledgerAccountId, amount, description, receiptReference,
                journalEntryId);
    }

    public ExpenseId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LedgerAccountId getLedgerAccountId() {
        return ledgerAccountId;
    }

    public Amount getAmount() {
        return amount;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<String> getReceiptReference() {
        return Optional.ofNullable(receiptReference);
    }

    public JournalEntryId getJournalEntryId() {
        return journalEntryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Expense other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
