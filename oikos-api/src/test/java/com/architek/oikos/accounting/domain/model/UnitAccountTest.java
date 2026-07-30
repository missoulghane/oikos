package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class UnitAccountTest {

    @Test
    void create_starts_with_a_zero_balance() {
        UnitAccount account = UnitAccount.create(UnitAccountId.newId(), EntityId.newId(), EntityId.newId(), Instant.now());
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void applying_a_debit_decreases_the_balance() {
        UnitAccount account = UnitAccount.create(UnitAccountId.newId(), EntityId.newId(), EntityId.newId(), Instant.now());

        UnitAccount debited = account.applyMovement(UnitAccountMovementDirection.DEBIT, new BigDecimal("300"), Instant.now());

        assertThat(debited.getBalance()).isEqualByComparingTo("-300");
    }

    @Test
    void applying_a_credit_larger_than_the_debt_yields_a_positive_advance_balance() {
        UnitAccount account = UnitAccount.create(UnitAccountId.newId(), EntityId.newId(), EntityId.newId(), Instant.now())
                .applyMovement(UnitAccountMovementDirection.DEBIT, new BigDecimal("50"), Instant.now());

        UnitAccount paid = account.applyMovement(UnitAccountMovementDirection.CREDIT, new BigDecimal("150"), Instant.now());

        assertThat(paid.getBalance()).isEqualByComparingTo("100");
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        UnitAccountId id = UnitAccountId.newId();
        UnitAccount a = UnitAccount.create(id, EntityId.newId(), EntityId.newId(), Instant.now());
        UnitAccount b = UnitAccount.reconstruct(id, EntityId.newId(), EntityId.newId(), new BigDecimal("999"), Instant.now());

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(UnitAccount.create(UnitAccountId.newId(), EntityId.newId(), EntityId.newId(), Instant.now()));
    }
}
