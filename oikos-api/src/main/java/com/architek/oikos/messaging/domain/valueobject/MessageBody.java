package com.architek.oikos.messaging.domain.valueobject;

import java.util.Objects;

/**
 * Text content of a Message - not-blank, capped at 4000 characters (mirrored
 * declaratively on the web layer by SendMessageRequest's Bean Validation
 * annotations, since a request DTO can't reuse a domain VO's constructor as
 * its own validation without pulling Jackson/Spring into domain).
 */
public record MessageBody(String value) {

    private static final int MAX_LENGTH = 4000;

    public MessageBody {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("body must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("body must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static MessageBody of(String value) {
        return new MessageBody(value);
    }
}
