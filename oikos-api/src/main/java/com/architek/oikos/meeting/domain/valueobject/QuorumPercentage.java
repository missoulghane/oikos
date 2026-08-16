package com.architek.oikos.meeting.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Share of the copropriété's voices that must be present for the session to
 * open legally, as a percentage between 0 and 100.
 *
 * <p>{@link #none()} - zero - is the default when a property has never
 * configured a threshold for a given meeting type. Deliberately not a legal
 * default: inventing "50%" would silently make every meeting of every
 * property subject to a rule nobody chose, and a wrong quorum is precisely
 * the kind of defect that voids decisions. No threshold means the check
 * always passes, and the syndic sees an unconfigured quorum for what it is.
 */
public record QuorumPercentage(BigDecimal value) {

    private static final BigDecimal MIN = BigDecimal.ZERO;
    private static final BigDecimal MAX = BigDecimal.valueOf(100);
    private static final int SCALE = 2;

    public QuorumPercentage {
        Objects.requireNonNull(value, "value must not be null");
        if (value.compareTo(MIN) < 0 || value.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("quorum percentage must be between 0 and 100, was: " + value);
        }
        value = value.setScale(SCALE, java.math.RoundingMode.HALF_UP);
    }

    public static QuorumPercentage of(BigDecimal value) {
        return new QuorumPercentage(value);
    }

    public static QuorumPercentage none() {
        return new QuorumPercentage(BigDecimal.ZERO);
    }

    public boolean isRequired() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * True when the voices actually present reach the threshold. Guarded
     * against a total of zero - a copropriété with no lot at all would
     * otherwise divide by zero here rather than fail where the real problem
     * is.
     */
    public boolean isReachedBy(BigDecimal presentWeight, BigDecimal totalWeight) {
        Objects.requireNonNull(presentWeight, "presentWeight must not be null");
        Objects.requireNonNull(totalWeight, "totalWeight must not be null");
        if (!isRequired()) {
            return true;
        }
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal reached = presentWeight.multiply(MAX).divide(totalWeight, SCALE + 2, java.math.RoundingMode.HALF_UP);
        return reached.compareTo(value) >= 0;
    }
}
