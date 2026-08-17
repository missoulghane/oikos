package com.architek.oikos.meeting.domain.valueobject;

import java.util.Locale;
import java.util.Objects;

/**
 * How an answer physically reached the office - by phone, by post, at the
 * counter - as a value.
 *
 * <p>A catalog row rather than an enum, unlike {@link ReplySource}, and the two
 * carry different kinds of fact. The source is a chemin de code the server
 * deduces and nobody can claim; the medium is a piece of information the syndic
 * declares about a conversation the application never saw. Nothing branches on
 * it, so a new one is an INSERT - the same reason the convocation channels
 * became data.
 */
public record ReplyMediumCode(String value) {

    public ReplyMediumCode {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("reply medium code must not be blank");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
    }

    public static ReplyMediumCode of(String value) {
        return new ReplyMediumCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
