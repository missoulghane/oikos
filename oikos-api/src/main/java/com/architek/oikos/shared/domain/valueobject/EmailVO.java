package com.architek.oikos.shared.domain.valueobject;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email value object. Normalized to lowercase so equals/hashCode compare on the
 * canonical value regardless of the casing the caller supplied.
 */
public record EmailVO(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailVO {
        Objects.requireNonNull(value, "email must not be null");
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        value = normalized;
    }

    public static EmailVO of(String raw) {
        return new EmailVO(raw);
    }
}
