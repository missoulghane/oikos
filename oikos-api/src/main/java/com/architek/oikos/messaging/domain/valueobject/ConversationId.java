package com.architek.oikos.messaging.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ConversationId(EntityId value) {

    public ConversationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ConversationId newId() {
        return new ConversationId(EntityId.newId());
    }

    public static ConversationId of(UUID value) {
        return new ConversationId(EntityId.of(value));
    }

    public static ConversationId of(String value) {
        return new ConversationId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
