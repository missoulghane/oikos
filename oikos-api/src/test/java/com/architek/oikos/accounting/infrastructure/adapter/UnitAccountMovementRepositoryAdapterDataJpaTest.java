package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitAccountMovementRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.UnitAccountMovementPersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class UnitAccountMovementRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitAccountMovementRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_a_movement() {
        UnitAccountId unitAccountId = UnitAccountId.newId();
        UnitAccountMovement saved = adapter.save(UnitAccountMovement.create(UnitAccountMovementId.newId(),
                AccountingExerciseId.newId(), unitAccountId, LocalDate.of(2026, 2, 5), UnitAccountMovementType.FUND_CALL,
                UnitAccountMovementDirection.DEBIT, Amount.of(new BigDecimal("200")), "installment-ref",
                "Appel de cotisation", null));

        UnitAccountMovement reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getUnitAccountId()).isEqualTo(unitAccountId);
        assertThat(reloaded.getAmount().value()).isEqualByComparingTo("200");
        assertThat(reloaded.getBusinessReference()).isEqualTo("installment-ref");
    }

    @Test
    void lists_movements_of_a_unit_account_paginated_and_unpaginated() {
        UnitAccountId unitAccountId = UnitAccountId.newId();
        adapter.save(UnitAccountMovement.create(UnitAccountMovementId.newId(), AccountingExerciseId.newId(),
                unitAccountId, LocalDate.of(2026, 1, 5), UnitAccountMovementType.FUND_CALL,
                UnitAccountMovementDirection.DEBIT, Amount.of(new BigDecimal("200")), null, "Appel", null));
        adapter.save(UnitAccountMovement.create(UnitAccountMovementId.newId(), AccountingExerciseId.newId(),
                unitAccountId, LocalDate.of(2026, 1, 10), UnitAccountMovementType.PAYMENT,
                UnitAccountMovementDirection.CREDIT, Amount.of(new BigDecimal("200")), null, "Paiement", null));

        assertThat(adapter.findAllByUnitAccountId(unitAccountId)).hasSize(2);
        assertThat(adapter.findPageByUnitAccountId(unitAccountId, PageRequest.of(0, 20)).content()).hasSize(2);
    }
}
