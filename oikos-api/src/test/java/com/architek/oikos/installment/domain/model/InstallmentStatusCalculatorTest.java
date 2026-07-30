package com.architek.oikos.installment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;

class InstallmentStatusCalculatorTest {

    @Test
    void nothing_settled_yet_is_not_settled() {
        InstallmentStatus status = InstallmentStatusCalculator.compute(new BigDecimal("300"), new BigDecimal("300"));
        assertThat(status).isEqualTo(InstallmentStatus.NOT_SETTLED);
    }

    @Test
    void fully_settled_is_settled() {
        InstallmentStatus status = InstallmentStatusCalculator.compute(new BigDecimal("300"), BigDecimal.ZERO);
        assertThat(status).isEqualTo(InstallmentStatus.SETTLED);
    }

    @Test
    void partially_settled_is_partially_settled() {
        InstallmentStatus status = InstallmentStatusCalculator.compute(new BigDecimal("300"), new BigDecimal("150"));
        assertThat(status).isEqualTo(InstallmentStatus.PARTIALLY_SETTLED);
    }
}
