package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AdvanceConsumptionCalculatorTest {

    @Test
    void golden_dataset_september_call_consumes_300_of_lot04_s_900_advance() {
        BigDecimal consumed = AdvanceConsumptionCalculator.consume(new BigDecimal("900.00"), new BigDecimal("300.00"));

        assertThat(consumed).isEqualByComparingTo("300.00");
    }

    @Test
    void consumption_is_capped_at_the_available_advance() {
        BigDecimal consumed = AdvanceConsumptionCalculator.consume(new BigDecimal("100.00"), new BigDecimal("300.00"));

        assertThat(consumed).isEqualByComparingTo("100.00");
    }
}
