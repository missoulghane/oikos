package com.architek.oikos.shared.domain.valueobject;

/**
 * Transient plain-text password. Never persisted and never logged: toString()
 * intentionally masks the value. Only {@link HashedPassword} is stored.
 */
public final class RawPassword {

    public static final int MIN_LENGTH = 10;

    private final String value;

    private RawPassword(String value) {
        this.value = value;
    }

    public static RawPassword of(String value) {
        if (value == null || value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        return new RawPassword(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof RawPassword other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "RawPassword[PROTECTED]";
    }
}
