package com.architek.oikos.accounting.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

/**
 * A single entry in a customer account's ledger. Immutable: a movement is
 * never updated nor physically deleted once created (RG002) - it is the
 * append-only source of truth the account balance is always computed from
 * (RG010). Allocations (see {@link Allocation}) never alter a movement.
 */
public final class Movement {

    private final MovementId id;
    private final AccountId accountId;
    private final Instant occurredOn;
    private final MovementType type;
    private final MovementDirection direction;
    private final Amount amount;
    private final String label;
    private final String businessReference;

    private Movement(MovementId id, AccountId accountId, Instant occurredOn, MovementType type,
                      MovementDirection direction, Amount amount, String label, String businessReference) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.occurredOn = Objects.requireNonNull(occurredOn, "occurredOn must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.label = requireNonBlank(label, "label");
        this.businessReference = businessReference;
    }

    public static Movement create(MovementId id, AccountId accountId, Instant occurredOn, MovementType type,
                                   MovementDirection direction, Amount amount, String label, String businessReference) {
        return new Movement(id, accountId, occurredOn, type, direction, amount, label, businessReference);
    }

    public static Movement reconstruct(MovementId id, AccountId accountId, Instant occurredOn, MovementType type,
                                        MovementDirection direction, Amount amount, String label, String businessReference) {
        return new Movement(id, accountId, occurredOn, type, direction, amount, label, businessReference);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public MovementId getId() {
        return id;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public MovementType getType() {
        return type;
    }

    public MovementDirection getDirection() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Movement other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
