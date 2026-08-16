package com.architek.oikos.meeting.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record VoteId(EntityId value) {

    public VoteId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static VoteId newId() {
        return new VoteId(EntityId.newId());
    }

    public static VoteId of(UUID value) {
        return new VoteId(EntityId.of(value));
    }

    public static VoteId of(String value) {
        return new VoteId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
