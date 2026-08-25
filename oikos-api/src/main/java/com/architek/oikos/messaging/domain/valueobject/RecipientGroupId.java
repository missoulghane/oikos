package com.architek.oikos.messaging.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecipientGroupId(EntityId value) {

    public RecipientGroupId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RecipientGroupId newId() {
        return new RecipientGroupId(EntityId.newId());
    }

    public static RecipientGroupId of(UUID value) {
        return new RecipientGroupId(EntityId.of(value));
    }

    public static RecipientGroupId of(String value) {
        return new RecipientGroupId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
