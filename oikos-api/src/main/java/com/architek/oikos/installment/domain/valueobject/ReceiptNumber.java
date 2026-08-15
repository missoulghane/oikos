package com.architek.oikos.installment.domain.valueobject;

import java.util.Objects;

/**
 * Human-readable reference of a payment receipt: REC-2026-0042.
 *
 * Scoped per (property, year) by the sequence that allocates it, which is the
 * usual accounting convention - each copropriété keeps its own series, and the
 * receipt names its copropriété, so two properties sharing REC-2026-0001 is not
 * ambiguous. The year is the payment's value date, not the generation date: a
 * receipt reprinted in January must keep the reference of the year it belongs to.
 *
 * Padded to four digits for readability only; a series that runs past 9999 keeps
 * counting rather than wrapping or truncating.
 */
public record ReceiptNumber(int year, int sequence) {

    public ReceiptNumber {
        if (year < 2000) {
            throw new IllegalArgumentException("year must be a four-digit year, got " + year);
        }
        if (sequence < 1) {
            throw new IllegalArgumentException("sequence must be >= 1, got " + sequence);
        }
    }

    /** Parses back the stored form; the inverse of {@link #format()}. */
    public static ReceiptNumber parse(String value) {
        Objects.requireNonNull(value, "value must not be null");
        String[] parts = value.split("-");
        if (parts.length != 3 || !"REC".equals(parts[0])) {
            throw new IllegalArgumentException("Not a receipt number: " + value);
        }
        return new ReceiptNumber(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public String format() {
        return "REC-%d-%04d".formatted(year, sequence);
    }

    @Override
    public String toString() {
        return format();
    }
}
