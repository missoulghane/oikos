package com.architek.oikos.installment.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.installment.domain.exception.InvalidInstallmentCallTransitionException;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Describes a single fund-collection event for a property over one month
 * (the period): generating one covers every unit of the property in a
 * single batch, one Installment (+ its triggering debit) per unit, priced
 * from that unit's UnitTypePricing. dueDate is kept distinct from period
 * (the month being billed) so payment can be due on a different date than
 * the period itself (e.g. the 5th of the following month).
 *
 * <p>Lifecycle (spec &sect;4.1/P1): DRAFT -&gt; ISSUED ("emission", ventilation
 * frozen) -&gt; POSTED ("comptabilisation", journalEntryId set once the VT
 * journal entry - accounting's aggregate, referenced only by its generic
 * EntityId per the cross-module convention - has been generated, a later
 * phase). create(...) still defaults to POSTED with no journalEntryId,
 * matching today's actual behaviour (GenerateInstallmentCallService/
 * RecordInstallmentCallService already charge units immediately, with no
 * separate review step yet) - draft(...) is for the full P1 flow a later
 * phase will wire up. journalEntryId is a generic EntityId, never
 * accounting's own JournalEntryId (cross-module convention, see
 * docs/NOMENCLATURE.md).
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class InstallmentCall {

    private final InstallmentCallId id;
    private final EntityId propertyId;
    private final YearMonth period;
    private final LocalDate dueDate;
    private final InstallmentCallStatus status;
    private final EntityId journalEntryId;

    private InstallmentCall(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate,
                             InstallmentCallStatus status, EntityId journalEntryId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.period = Objects.requireNonNull(period, "period must not be null");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.journalEntryId = journalEntryId;
    }

    public static InstallmentCall create(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate) {
        return new InstallmentCall(id, propertyId, period, dueDate, InstallmentCallStatus.POSTED, null);
    }

    public static InstallmentCall draft(InstallmentCallId id, EntityId propertyId, YearMonth period, LocalDate dueDate) {
        return new InstallmentCall(id, propertyId, period, dueDate, InstallmentCallStatus.DRAFT, null);
    }

    public static InstallmentCall reconstruct(InstallmentCallId id, EntityId propertyId, YearMonth period,
                                               LocalDate dueDate, InstallmentCallStatus status, EntityId journalEntryId) {
        return new InstallmentCall(id, propertyId, period, dueDate, status, journalEntryId);
    }

    /** P1 "emission": freezes the ventilation. */
    public InstallmentCall issue() {
        if (status != InstallmentCallStatus.DRAFT) {
            throw new InvalidInstallmentCallTransitionException(id, status, "be issued (must be DRAFT)");
        }
        return new InstallmentCall(id, propertyId, period, dueDate, InstallmentCallStatus.ISSUED, null);
    }

    /** P1 "comptabilisation": records the journal entry generated for this call. */
    public InstallmentCall post(EntityId journalEntryId) {
        if (status != InstallmentCallStatus.ISSUED) {
            throw new InvalidInstallmentCallTransitionException(id, status, "be posted (must be ISSUED)");
        }
        return new InstallmentCall(id, propertyId, period, dueDate, InstallmentCallStatus.POSTED,
                Objects.requireNonNull(journalEntryId, "journalEntryId must not be null"));
    }

    /** Direct cancellation is only valid before posting; a POSTED call needs a contre-passation (a later phase). */
    public InstallmentCall cancel() {
        if (status == InstallmentCallStatus.POSTED) {
            throw new InvalidInstallmentCallTransitionException(id, status,
                    "be cancelled directly (already posted - requires a contre-passation)");
        }
        if (status == InstallmentCallStatus.CANCELLED) {
            throw new InvalidInstallmentCallTransitionException(id, status, "be cancelled again");
        }
        return new InstallmentCall(id, propertyId, period, dueDate, InstallmentCallStatus.CANCELLED, null);
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

    public InstallmentCallStatus getStatus() {
        return status;
    }

    public Optional<EntityId> getJournalEntryId() {
        return Optional.ofNullable(journalEntryId);
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
