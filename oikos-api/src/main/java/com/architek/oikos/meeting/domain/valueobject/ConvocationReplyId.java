package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ConvocationReplyId(EntityId value) {

    public ConvocationReplyId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ConvocationReplyId newId() {
        return new ConvocationReplyId(EntityId.newId());
    }

    public static ConvocationReplyId of(UUID value) {
        return new ConvocationReplyId(EntityId.of(value));
    }

    public static ConvocationReplyId of(String value) {
        return new ConvocationReplyId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
