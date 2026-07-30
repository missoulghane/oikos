package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;

class UnitAccountMovementTest {

    @Test
    void a_regularization_requires_a_reason() {
        assertThatThrownBy(() -> UnitAccountMovement.create(UnitAccountMovementId.newId(), AccountingExerciseId.newId(),
                UnitAccountId.newId(), LocalDate.now(), UnitAccountMovementType.REGULARIZATION,
                UnitAccountMovementDirection.CREDIT, Amount.of(new java.math.BigDecimal("50")), null, "Remise", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void a_fund_call_must_not_carry_a_reason() {
        assertThatThrownBy(() -> UnitAccountMovement.create(UnitAccountMovementId.newId(), AccountingExerciseId.newId(),
                UnitAccountId.newId(), LocalDate.now(), UnitAccountMovementType.FUND_CALL,
                UnitAccountMovementDirection.DEBIT, Amount.of(new java.math.BigDecimal("200")), null, "Appel de cotisation",
                "not allowed here"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void a_regularization_with_a_reason_is_created_successfully() {
        UnitAccountMovement movement = UnitAccountMovement.create(UnitAccountMovementId.newId(), AccountingExerciseId.newId(),
                UnitAccountId.newId(), LocalDate.now(), UnitAccountMovementType.REGULARIZATION,
                UnitAccountMovementDirection.CREDIT, Amount.of(new java.math.BigDecimal("50")), null, "Remise",
                "Geste commercial");

        assertThat(movement.getReason()).isEqualTo("Geste commercial");
    }

    @Test
    void equality_is_based_on_identity_not_on_field_values() {
        UnitAccountMovementId id = UnitAccountMovementId.newId();
        UnitAccountMovement a = UnitAccountMovement.create(id, AccountingExerciseId.newId(), UnitAccountId.newId(),
                LocalDate.now(), UnitAccountMovementType.PAYMENT, UnitAccountMovementDirection.CREDIT,
                Amount.of(new java.math.BigDecimal("10")), null, "Paiement", null);
        UnitAccountMovement b = UnitAccountMovement.reconstruct(id, AccountingExerciseId.newId(), UnitAccountId.newId(),
                LocalDate.now(), UnitAccountMovementType.PAYMENT, UnitAccountMovementDirection.CREDIT,
                Amount.of(new java.math.BigDecimal("999")), null, "Autre libelle", null);

        assertThat(a).isEqualTo(b);
    }
}
