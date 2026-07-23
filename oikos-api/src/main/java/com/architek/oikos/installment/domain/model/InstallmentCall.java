package com.architek.oikos.installment.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Describes a single fund-collection event for a property over one month
 * (the period): generating one covers every unit of the property in a
 * single batch, one Installment (+ its triggering Movement) per unit, priced
 * from that unit's UnitTypePricing. dueDate is kept distinct from period
 * (the month being billed) so payment can be due on a different date than
 * the period itself (e.g. the 5th of the following month). Immutable: every
 * mutation returns a new instance. Entity semantics: equals/hashCode are
 * identity-based (on id), not value-based.
 */
public final class InstallmentCall {

    private final InstallmentCallId id;
    private final EntityId propertyId;
    private final YearMonth period;
    private final LocalDate dueDate;

    private InstallmentCall(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.period = Objects.requireNonNull(period, "period must not be null");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
    }

    public static InstallmentCall create(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate) {
        return new InstallmentCall(id, propertyId, period, dueDate);
    }

    public static InstallmentCall reconstruct(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate) {
        return new InstallmentCall(id, propertyId, period, dueDate);
    }

    public InstallmentCallId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public YearMonth getPeriod() {
        return period;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof InstallmentCall other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
