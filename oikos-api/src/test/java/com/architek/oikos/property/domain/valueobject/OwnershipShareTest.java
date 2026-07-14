package com.architek.oikos.property.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class OwnershipShareTest {

    @Test
    void accepts_values_between_0_and_100() {
        assertThat(OwnershipShare.of(BigDecimal.ZERO).value()).isEqualByComparingTo("0");
        assertThat(OwnershipShare.of(new BigDecimal("100")).value()).isEqualByComparingTo("100");
        assertThat(OwnershipShare.of(new BigDecimal("50.5")).value()).isEqualByComparingTo("50.5");
    }

    @Test
    void rejects_a_negative_value() {
        assertThatThrownBy(() -> OwnershipShare.of(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_value_above_100() {
        assertThatThrownBy(() -> OwnershipShare.of(new BigDecimal("100.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
