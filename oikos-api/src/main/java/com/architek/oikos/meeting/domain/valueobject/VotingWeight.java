package com.architek.oikos.meeting.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The weight of one lot's voice, snapshotted on its convocation: 1 under
 * PER_UNIT, the lot's tantièmes under SHARES.
 *
 * <p>Zero is allowed - a lot whose tantièmes have not been assigned yet
 * (ConfigurePropertyService creates units with zero shares) is convoked like
 * any other, it simply weighs nothing in the count. Refusing it here would
 * block the convocation of a whole copropriété over a data-entry gap.
 */
public record VotingWeight(BigDecimal value) {

    private static final int SCALE = 2;

    public VotingWeight {
        Objects.requireNonNull(value, "value must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("voting weight must not be negative, was: " + value);
        }
        value = value.setScale(SCALE, java.math.RoundingMode.HALF_UP);
    }

    public static VotingWeight of(BigDecimal value) {
        return new VotingWeight(value);
    }

    /** One lot, one voice. */
    public static VotingWeight perUnit() {
        return new VotingWeight(BigDecimal.ONE);
    }

    public static VotingWeight forMode(VotingWeightMode mode, BigDecimal shares) {
        return mode == VotingWeightMode.SHARES ? of(shares) : perUnit();
    }

    public VotingWeight plus(VotingWeight other) {
        return new VotingWeight(value.add(other.value));
    }

    public static VotingWeight zero() {
        return new VotingWeight(BigDecimal.ZERO);
    }
}
