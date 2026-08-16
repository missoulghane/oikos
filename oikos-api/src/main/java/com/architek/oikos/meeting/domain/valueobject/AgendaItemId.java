package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AgendaItemId(EntityId value) {

    public AgendaItemId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AgendaItemId newId() {
        return new AgendaItemId(EntityId.newId());
    }

    public static AgendaItemId of(UUID value) {
        return new AgendaItemId(EntityId.of(value));
    }

    public static AgendaItemId of(String value) {
        return new AgendaItemId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
