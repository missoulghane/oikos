package com.architek.oikos.accounting.domain.model;

import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One debit or credit line of a JournalEntry (spec &sect;4.1). The auxiliary
 * is a first-class field here - a real UnitId or PartyId, never derived from
 * the account number (ADR 0001 &sect;4.3 anti-derivation principle). I3
 * (strictly positive amount) is enforced by the Amount value object, not
 * repeated here. Never constructed or persisted on its own outside a
 * JournalEntry (no independent repository).
 * Immutable value-ish entity: equals/hashCode are identity-based (on id).
 */
public final class JournalEntryLine {

    private final JournalEntryLineId id;
    private final LedgerAccountId ledgerAccountId;
    private final EntityId auxiliaryUnitId;
    private final EntityId auxiliaryPartyId;
    private final EntryDirection direction;
    private final Amount amount;
    private final String label;

    private JournalEntryLine(JournalEntryLineId id, LedgerAccountId ledgerAccountId, EntityId auxiliaryUnitId,
                              EntityId auxiliaryPartyId, EntryDirection direction, Amount amount, String label) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.ledgerAccountId = Objects.requireNonNull(ledgerAccountId, "ledgerAccountId must not be null");
        if (auxiliaryUnitId != null && auxiliaryPartyId != null) {
            throw new IllegalArgumentException("a line cannot carry both a unit and a party auxiliary");
        }
        this.auxiliaryUnitId = auxiliaryUnitId;
        this.auxiliaryPartyId = auxiliaryPartyId;
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.label = requireNonBlank(label, "label");
    }

    public static JournalEntryLine of(JournalEntryLineId id, LedgerAccountId ledgerAccountId,
                                       EntityId auxiliaryUnitId, EntityId auxiliaryPartyId, EntryDirection direction,
                                       Amount amount, String label) {
        return new JournalEntryLine(id, ledgerAccountId, auxiliaryUnitId, auxiliaryPartyId, direction, amount, label);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public boolean hasAuxiliary() {
        return auxiliaryUnitId != null || auxiliaryPartyId != null;
    }

    public JournalEntryLineId getId() {
        return id;
    }

    public LedgerAccountId getLedgerAccountId() {
        return ledgerAccountId;
    }

    public Optional<EntityId> getAuxiliaryUnitId() {
        return Optional.ofNullable(auxiliaryUnitId);
    }

    public Optional<EntityId> getAuxiliaryPartyId() {
        return Optional.ofNullable(auxiliaryPartyId);
    }

    public EntryDirection getDirection() {
        return direction;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof JournalEntryLine other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
