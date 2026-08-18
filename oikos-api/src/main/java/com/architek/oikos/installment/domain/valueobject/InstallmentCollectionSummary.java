package com.architek.oikos.installment.domain.valueobject;

import java.math.BigDecimal;

/**
 * What a copropriété still has to collect: how many installments are unpaid and
 * already due, and how much they add up to.
 *
 * <p>"Unpaid" here is {@link InstallmentStatus#NOT_SETTLED} - nothing received
 * on it - and not "anything with an outstanding balance". That is the same set
 * the tracking screen shows under its "Non soldée" filter, which is where the
 * figure links to: a badge counting one set and opening a list of another is
 * read as a bug, whichever of the two is the better accounting question.
 *
 * <p>"Already due" is the counterpart of the screen's "à échoir" switch left
 * off: money owed now, not money that will be owed.
 */
public record InstallmentCollectionSummary(long count, BigDecimal amount) {

    public static final InstallmentCollectionSummary EMPTY =
            new InstallmentCollectionSummary(0L, BigDecimal.ZERO);

    public InstallmentCollectionSummary {
        amount = amount == null ? BigDecimal.ZERO : amount;
    }
}
