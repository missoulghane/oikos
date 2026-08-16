package com.architek.oikos.meeting.domain.valueobject;

import java.util.Locale;
import java.util.Objects;

/**
 * A six-character code as it is stored and compared - a convocation's
 * confirmation code, or a meeting's public reference.
 *
 * <p>Normalises on the way in, and that is the whole point of the type: it is
 * read off paper and typed by hand, so "W754A1", " w754a1 " and "w754a1" are
 * the same code. Rejecting the first two would be blaming a copropriétaire for
 * their keyboard.
 *
 * <p>Deliberately does not validate the alphabet. A code that contains an `l`
 * simply matches nothing, and refusing it with a different error would tell an
 * attacker which characters are worth trying.
 */
public record ShortCode(String value) {

    private static final int LENGTH = 6;

    public ShortCode {
        Objects.requireNonNull(value, "value must not be null");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() != LENGTH) {
            throw new IllegalArgumentException("a short code is exactly " + LENGTH + " characters");
        }
    }

    public static ShortCode of(String value) {
        return new ShortCode(value);
    }

    /** Null-tolerant: a blank field on a public form is "no code", not a malformed one. */
    public static ShortCode ofNullable(String value) {
        return value == null || value.isBlank() ? null : new ShortCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
