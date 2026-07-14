package com.architek.oikos.shared.domain.valueobject;

import java.util.Objects;

/**
 * Result of {@code PasswordEncoderPort#encode(RawPassword)}. This is the only
 * password representation ever persisted.
 */
public record HashedPassword(String value) {

    public HashedPassword {
        Objects.requireNonNull(value, "hashed password must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("hashed password must not be blank");
        }
    }

    public static HashedPassword of(String encodedValue) {
        return new HashedPassword(encodedValue);
    }

    @Override
    public String toString() {
        return "HashedPassword[PROTECTED]";
    }
}
