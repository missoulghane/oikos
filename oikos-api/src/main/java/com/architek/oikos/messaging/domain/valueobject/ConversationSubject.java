package com.architek.oikos.messaging.domain.valueobject;

import java.util.Objects;

/**
 * Title of a GROUP conversation (email-style "subject"), set once at
 * compose time - not-blank, capped at 200 characters. A BROADCAST
 * conversation never has one (see Conversation's javadoc): its identity is
 * already the fixed "Annonces de la copropriété"-style channel label, not a
 * per-message subject.
 */
public record ConversationSubject(String value) {

    private static final int MAX_LENGTH = 200;

    public ConversationSubject {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("subject must not exceed " + MAX_LENGTH + " characters");
        }
    }

    public static ConversationSubject of(String value) {
        return new ConversationSubject(value);
    }
}
