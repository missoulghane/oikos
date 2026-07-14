package com.architek.oikos.contact.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ContactId(EntityId value) {

    public ContactId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ContactId newId() {
        return new ContactId(EntityId.newId());
    }

    public static ContactId of(UUID value) {
        return new ContactId(EntityId.of(value));
    }

    public static ContactId of(String value) {
        return new ContactId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
