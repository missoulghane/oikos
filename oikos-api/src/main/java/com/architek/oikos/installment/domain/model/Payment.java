package com.architek.oikos.installment.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * An owner payment ("Reglement", spec &sect;4.1/P2/P3), always created
 * together with the journal entry it triggers (journalEntryId is required,
 * like the pre-PCM Expense.journalEntryId) - split into an "imputee"
 * portion (against the unit's unsettled fund calls) and an "avance" portion
 * via PaymentAllocationCalculator, never computed here (a payment itself
 * does not know how it was split - that split is recorded as JournalEntry
 * lines and Allocation rows by the use case that creates it, a later
 * phase). propertyId/unitId/journalEntryId are generic EntityId, never
 * another module's own strongly-typed id (cross-module convention, see
 * docs/NOMENCLATURE.md).
 * Immutable: entity semantics, equals/hashCode are identity-based (on id).
 */
public final class Payment {

    private final PaymentId id;
    private final EntityId propertyId;
    private final EntityId unitId;
    private final PaymentMode mode;
    private final LocalDate valueDate;
    private final Amount amount;
    private final EntityId journalEntryId;

    private Payment(PaymentId id, EntityId propertyId, EntityId unitId, PaymentMode mode, LocalDate valueDate,
                     Amount amount, EntityId journalEntryId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        this.valueDate = Objects.requireNonNull(valueDate, "valueDate must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.journalEntryId = Objects.requireNonNull(journalEntryId, "journalEntryId must not be null");
    }

    public static Payment create(PaymentId id, EntityId propertyId, EntityId unitId, PaymentMode mode,
                                  LocalDate valueDate, Amount amount, EntityId journalEntryId) {
        return new Payment(id, propertyId, unitId, mode, valueDate, amount, journalEntryId);
    }

    public static Payment reconstruct(PaymentId id, EntityId propertyId, EntityId unitId, PaymentMode mode,
                                       LocalDate valueDate, Amount amount, EntityId journalEntryId) {
        return new Payment(id, propertyId, unitId, mode, valueDate, amount, journalEntryId);
    }

    public PaymentId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public PaymentMode getMode() {
        return mode;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public Amount getAmount() {
        return amount;
    }

    public EntityId getJournalEntryId() {
        return journalEntryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Payment other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
