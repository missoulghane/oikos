package com.architek.oikos.messaging.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MessageId(EntityId value) {

    public MessageId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MessageId newId() {
        return new MessageId(EntityId.newId());
    }

    public static MessageId of(UUID value) {
        return new MessageId(EntityId.of(value));
    }

    public static MessageId of(String value) {
        return new MessageId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
