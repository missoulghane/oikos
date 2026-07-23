package com.architek.oikos.property.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PriceTest {

    @Test
    void zero_is_a_valid_price() {
        Price price = Price.of(BigDecimal.ZERO);

        assertThat(price.value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void negative_value_is_rejected() {
        assertThatThrownBy(() -> Price.of(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
