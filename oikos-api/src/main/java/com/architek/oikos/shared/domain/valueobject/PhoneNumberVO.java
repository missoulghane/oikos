package com.architek.oikos.shared.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Phone number value object, normalized to the international digits-only form
 * (country code first, no '+', no '00', no separator). That form is exactly what
 * the messaging providers expect - Vonage rejects a leading '+' or '00' outright -
 * so the normalization lives here rather than in each adapter, where the next
 * channel added would have had to rediscover the rule.
 */
public record PhoneNumberVO(String value) {

    /** E.164 : indicatif pays compris, 15 chiffres au maximum. */
    private static final Pattern INTERNATIONAL_PATTERN = Pattern.compile("^[1-9]\\d{7,14}$");
    private static final Pattern SEPARATORS = Pattern.compile("[\\s.\\-()/]");

    public PhoneNumberVO {
        Objects.requireNonNull(value, "phone number must not be null");
        String normalized = SEPARATORS.matcher(value.trim()).replaceAll("");
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("00")) {
            normalized = normalized.substring(2);
        }
        if (!INTERNATIONAL_PATTERN.matcher(normalized).matches()) {
            // Un numéro national (« 0612... ») n'est pas rattrapable ici : il faudrait
            // deviner le pays, et se tromper enverrait le message à un inconnu.
            throw new IllegalArgumentException("Invalid international phone number: " + value);
        }
        value = normalized;
    }

    public static PhoneNumberVO of(String raw) {
        return new PhoneNumberVO(raw);
    }
}
