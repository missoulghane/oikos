package com.architek.oikos.user.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record VerificationTokenId(EntityId value) {

    public VerificationTokenId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static VerificationTokenId newId() {
        return new VerificationTokenId(EntityId.newId());
    }

    public static VerificationTokenId of(UUID value) {
        return new VerificationTokenId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
