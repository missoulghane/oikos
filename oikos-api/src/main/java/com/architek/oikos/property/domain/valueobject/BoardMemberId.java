package com.architek.oikos.property.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record BoardMemberId(EntityId value) {

    public BoardMemberId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static BoardMemberId newId() {
        return new BoardMemberId(EntityId.newId());
    }

    public static BoardMemberId of(UUID value) {
        return new BoardMemberId(EntityId.of(value));
    }

    public static BoardMemberId of(String value) {
        return new BoardMemberId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
