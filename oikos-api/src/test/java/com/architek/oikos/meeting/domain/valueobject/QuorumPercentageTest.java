package com.architek.oikos.meeting.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class QuorumPercentageTest {

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    void an_unconfigured_quorum_is_zero_and_requires_nothing() {
        QuorumPercentage none = QuorumPercentage.none();

        assertThat(none.isRequired()).isFalse();
        assertThat(none.isReachedBy(BigDecimal.ZERO, bd("1000"))).isTrue();
    }

    @Test
    void a_percentage_outside_zero_to_hundred_is_refused() {
        assertThatThrownBy(() -> QuorumPercentage.of(bd("-1"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuorumPercentage.of(bd("100.01"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void the_threshold_is_reached_exactly_at_the_boundary() {
        QuorumPercentage half = QuorumPercentage.of(bd("50"));

        assertThat(half.isReachedBy(bd("500"), bd("1000"))).isTrue();
        assertThat(half.isReachedBy(bd("499.99"), bd("1000"))).isFalse();
        assertThat(half.isReachedBy(bd("500.01"), bd("1000"))).isTrue();
    }

    @Test
    void tantiemes_that_do_not_divide_evenly_are_still_compared_faithfully() {
        QuorumPercentage third = QuorumPercentage.of(bd("33.33"));

        // 1/3 of 3 lots = 33.3333...%, above the 33.33% threshold rather than rounded down onto it.
        assertThat(third.isReachedBy(BigDecimal.ONE, bd("3"))).isTrue();
        assertThat(QuorumPercentage.of(bd("33.34")).isReachedBy(BigDecimal.ONE, bd("3"))).isFalse();
    }

    @Test
    void a_copropriete_with_no_voices_at_all_never_reaches_a_required_quorum() {
        // Guards the division rather than blowing up: the real defect is a property without lots,
        // and it must not surface here as an arithmetic error.
        assertThat(QuorumPercentage.of(bd("50")).isReachedBy(BigDecimal.ZERO, BigDecimal.ZERO)).isFalse();
    }

    @Test
    void percentages_are_compared_on_value_whatever_their_scale() {
        assertThat(QuorumPercentage.of(bd("50"))).isEqualTo(QuorumPercentage.of(bd("50.00")));
    }
}
