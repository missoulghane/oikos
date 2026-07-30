package com.architek.oikos.accounting.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A single entry in the property's treasury journal (spec &sect;5) - every
 * operation with a real impact on cash/bank money. Immutable: a journal entry
 * is never updated nor physically deleted once created (spec &sect;17) - it
 * is the official history of the treasury.
 */
public final class FinancialJournalEntry {

    private final FinancialJournalEntryId id;
    private final AccountingExerciseId exerciseId;
    private final FinancialAccountId financialAccountId;
    private final LocalDate date;
    private final FinancialEntryType type;
    private final FinancialEntryDirection direction;
    private final Amount amount;
    private final String label;
    private final String businessReference;
    private final EntityId createdByUserId;

    private FinancialJournalEntry(FinancialJournalEntryId id, AccountingExerciseId exerciseId,
                                   FinancialAccountId financialAccountId, LocalDate date, FinancialEntryType type,
                                   FinancialEntryDirection direction, Amount amount, String label,
                                   String businessReference, EntityId createdByUserId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.exerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        this.financialAccountId = Objects.requireNonNull(financialAccountId, "financialAccountId must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.label = requireNonBlank(label, "label");
        this.businessReference = businessReference;
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
    }

    public static FinancialJournalEntry create(FinancialJournalEntryId id, AccountingExerciseId exerciseId,
                                                FinancialAccountId financialAccountId, LocalDate date,
                                                FinancialEntryType type, FinancialEntryDirection direction,
                                                Amount amount, String label, String businessReference,
                                                EntityId createdByUserId) {
        return new FinancialJournalEntry(id, exerciseId, financialAccountId, date, type, direction, amount, label,
                businessReference, createdByUserId);
    }

    public static FinancialJournalEntry reconstruct(FinancialJournalEntryId id, AccountingExerciseId exerciseId,
                                                     FinancialAccountId financialAccountId, LocalDate date,
                                                     FinancialEntryType type, FinancialEntryDirection direction,
                                                     Amount amount, String label, String businessReference,
                                                     EntityId createdByUserId) {
        return new FinancialJournalEntry(id, exerciseId, financialAccountId, date, type, direction, amount, label,
                businessReference, createdByUserId);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public FinancialJournalEntryId getId() {
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

    public FinancialEntryType getType() {
        return type;
    }

    public FinancialEntryDirection getDirection() {
        return direction;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }

    public String getBusinessReference() {
        return businessReference;
    }

    public EntityId getCreatedByUserId() {
        return createdByUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof FinancialJournalEntry other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
