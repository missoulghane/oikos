package com.architek.oikos.messaging.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MessageDraftId(EntityId value) {

    public MessageDraftId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static MessageDraftId newId() {
        return new MessageDraftId(EntityId.newId());
    }

    public static MessageDraftId of(UUID value) {
        return new MessageDraftId(EntityId.of(value));
    }

    public static MessageDraftId of(String value) {
        return new MessageDraftId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
