package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ConvocationDeliveryId(EntityId value) {

    public ConvocationDeliveryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ConvocationDeliveryId newId() {
        return new ConvocationDeliveryId(EntityId.newId());
    }

    public static ConvocationDeliveryId of(UUID value) {
        return new ConvocationDeliveryId(EntityId.of(value));
    }

    public static ConvocationDeliveryId of(String value) {
        return new ConvocationDeliveryId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
