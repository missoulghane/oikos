package com.architek.oikos.accounting.domain.valueobject;

import java.util.Objects;

/**
 * PCM account number (spec &sect;3.1: 8 digits). Distinct from the account's
 * id, which is always a GUID ("exigence supplementaire") - this is business
 * data, never a technical key.
 */
public record AccountNumber(String value) {

    private static final int LENGTH = 8;

    public AccountNumber {
        Objects.requireNonNull(value, "value must not be null");
        if (value.length() != LENGTH || !value.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("account number must be exactly " + LENGTH + " digits: " + value);
        }
    }

    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }

    /**
     * Formats a property/unit-scoped account number as {@code prefix} followed
     * by {@code increment} zero-padded to fill the remaining digits (e.g.
     * prefix "516100" + increment 1 -&gt; "51610001"), per the numbering scheme
     * of the "exigence supplementaire". The increment itself is allocated by
     * the caller under a row lock (ledger_account_number_sequence, I7-style),
     * never computed here.
     */
    public static AccountNumber forSequence(String prefix, int increment) {
        Objects.requireNonNull(prefix, "prefix must not be null");
        int digitsLeft = LENGTH - prefix.length();
        if (digitsLeft <= 0) {
            throw new IllegalArgumentException("prefix must leave room for the increment: " + prefix);
        }
        int maxIncrement = (int) Math.pow(10, digitsLeft) - 1;
        if (increment < 1 || increment > maxIncrement) {
            throw new IllegalArgumentException(
                    "increment must be between 1 and " + maxIncrement + " for prefix " + prefix + ": " + increment);
        }
        String padded = String.format("%0" + digitsLeft + "d", increment);
        return new AccountNumber(prefix + padded);
    }
}
