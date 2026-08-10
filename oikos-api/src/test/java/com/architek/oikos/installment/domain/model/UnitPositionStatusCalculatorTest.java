package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.domain.valueobject.UnitPositionStatus;

class UnitPositionStatusCalculatorTest {

    @Test
    void golden_dataset_lot02_is_in_advance() {
        // creance 0, avance 700 -> net position -700
        assertThat(UnitPositionStatusCalculator.compute(new BigDecimal("-700.00")))
                .isEqualTo(UnitPositionStatus.IN_ADVANCE);
    }

    @Test
    void golden_dataset_lots_05_to_10_are_overdue() {
        // creance 300, avance 0 -> net position +300
        assertThat(UnitPositionStatusCalculator.compute(new BigDecimal("300.00")))
                .isEqualTo(UnitPositionStatus.OVERDUE);
    }

    @Test
    void golden_dataset_lots_01_and_03_are_up_to_date() {
        assertThat(UnitPositionStatusCalculator.compute(BigDecimal.ZERO)).isEqualTo(UnitPositionStatus.UP_TO_DATE);
    }
}
