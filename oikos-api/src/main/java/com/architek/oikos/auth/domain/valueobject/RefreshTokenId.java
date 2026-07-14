package com.architek.oikos.auth.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RefreshTokenId(EntityId value) {

    public RefreshTokenId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RefreshTokenId newId() {
        return new RefreshTokenId(EntityId.newId());
    }

    public static RefreshTokenId of(UUID value) {
        return new RefreshTokenId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
