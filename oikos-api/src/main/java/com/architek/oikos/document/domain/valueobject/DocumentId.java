package com.architek.oikos.document.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record DocumentId(EntityId value) {

    public DocumentId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static DocumentId newId() {
        return new DocumentId(EntityId.newId());
    }

    public static DocumentId of(UUID value) {
        return new DocumentId(EntityId.of(value));
    }

    public static DocumentId of(String value) {
        return new DocumentId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
