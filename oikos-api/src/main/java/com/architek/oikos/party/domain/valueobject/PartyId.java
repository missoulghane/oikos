package com.architek.oikos.party.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PartyId(EntityId value) {

    public PartyId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PartyId newId() {
        return new PartyId(EntityId.newId());
    }

    public static PartyId of(UUID value) {
        return new PartyId(EntityId.of(value));
    }

    public static PartyId of(String value) {
        return new PartyId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
