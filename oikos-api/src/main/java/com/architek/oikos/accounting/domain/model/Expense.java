package com.architek.oikos.accounting.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * A property expense (spec &sect;6): free-text category and provider (same
 * spirit as UnitTypeDefinition in property - no fixed enum, each property
 * names its own categories/providers). Validating an expense always creates
 * its triggering FinancialJournalEntry (OUT) - journalEntryId links back to
 * it. Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class Expense {

    private final ExpenseId id;
    private final AccountingExerciseId exerciseId;
    private final FinancialAccountId financialAccountId;
    private final LocalDate date;
    private final String category;
    private final String provider;
    private final Amount amount;
    private final String description;
    private final String receiptReference;
    private final FinancialJournalEntryId journalEntryId;

    private Expense(ExpenseId id, AccountingExerciseId exerciseId, FinancialAccountId financialAccountId,
                     LocalDate date, String category, String provider, Amount amount, String description,
                     String receiptReference, FinancialJournalEntryId journalEntryId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.exerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        this.financialAccountId = Objects.requireNonNull(financialAccountId, "financialAccountId must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.category = requireNonBlank(category, "category");
        this.provider = requireNonBlank(provider, "provider");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.description = description;
        this.receiptReference = receiptReference;
        this.journalEntryId = Objects.requireNonNull(journalEntryId, "journalEntryId must not be null");
    }

    public static Expense create(ExpenseId id, AccountingExerciseId exerciseId, FinancialAccountId financialAccountId,
                                  LocalDate date, String category, String provider, Amount amount,
                                  String description, String receiptReference, FinancialJournalEntryId journalEntryId) {
        return new Expense(id, exerciseId, financialAccountId, date, category, provider, amount, description,
                receiptReference, journalEntryId);
    }

    public static Expense reconstruct(ExpenseId id, AccountingExerciseId exerciseId,
                                       FinancialAccountId financialAccountId, LocalDate date, String category,
                                       String provider, Amount amount, String description, String receiptReference,
                                       FinancialJournalEntryId journalEntryId) {
        return new Expense(id, exerciseId, financialAccountId, date, category, provider, amount, description,
                receiptReference, journalEntryId);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public ExpenseId getId() {
        return id;
    }

    public AccountingExerciseId getExerciseId() {
        return exerciseId;
    }

    public FinancialAccountId getFinancialAccountId() {
        return financialAccountId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getProvider() {
        return provider;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public FinancialJournalEntryId getJournalEntryId() {
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
