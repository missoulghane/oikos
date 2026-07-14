package com.architek.oikos.auth.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record PasswordResetTokenId(EntityId value) {

    public PasswordResetTokenId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static PasswordResetTokenId newId() {
        return new PasswordResetTokenId(EntityId.newId());
    }

    public static PasswordResetTokenId of(UUID value) {
        return new PasswordResetTokenId(EntityId.of(value));
    }

    public UUID asUuid() {
        return value.value();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
