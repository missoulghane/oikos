package com.architek.oikos.shared.application.port.out;

import java.util.Objects;

/**
 * One reply button of an interactive WhatsApp message. {@code id} is what comes
 * back on the inbound webhook when the recipient taps it, {@code title} is what
 * they read - WhatsApp caps it at 20 characters and silently rejects the whole
 * message beyond that, hence the check here rather than a provider-side surprise.
 */
public record WhatsAppReplyButton(String id, String title) {

    public static final int MAX_TITLE_LENGTH = 20;
    public static final int MAX_ID_LENGTH = 256;

    public WhatsAppReplyButton {
        Objects.requireNonNull(id, "button id must not be null");
        Objects.requireNonNull(title, "button title must not be null");
        id = id.trim();
        title = title.trim();
        if (id.isEmpty() || id.length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException("Button id must be 1 to " + MAX_ID_LENGTH + " characters: " + id);
        }
        if (title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Button title must be 1 to " + MAX_TITLE_LENGTH + " characters: " + title);
        }
    }
}
