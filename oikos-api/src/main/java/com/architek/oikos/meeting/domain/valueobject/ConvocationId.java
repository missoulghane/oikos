package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ConvocationId(EntityId value) {

    public ConvocationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ConvocationId newId() {
        return new ConvocationId(EntityId.newId());
    }

    public static ConvocationId of(UUID value) {
        return new ConvocationId(EntityId.of(value));
    }

    public static ConvocationId of(String value) {
        return new ConvocationId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
