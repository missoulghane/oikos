package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountAllocationId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UnitAccountAllocationRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.UnitAccountAllocationPersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class UnitAccountAllocationRepositoryAdapterDataJpaTest {

    @Autowired
    private UnitAccountAllocationRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_an_allocation() {
        UnitAccountId unitAccountId = UnitAccountId.newId();
        UnitAccountMovementId debitMovementId = UnitAccountMovementId.newId();
        UnitAccountMovementId creditMovementId = UnitAccountMovementId.newId();

        UnitAccountAllocation saved = adapter.save(UnitAccountAllocation.create(UnitAccountAllocationId.newId(),
                unitAccountId, debitMovementId, creditMovementId, Amount.of(new BigDecimal("150")),
                LocalDate.of(2026, 3, 10), EntityId.newId()));

        UnitAccountAllocation reloaded = adapter.findAllByUnitAccountId(unitAccountId).stream()
                .filter(a -> a.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(reloaded.getDebitMovementId()).isEqualTo(debitMovementId);
        assertThat(reloaded.getCreditMovementId()).isEqualTo(creditMovementId);
        assertThat(reloaded.getAmount().value()).isEqualByComparingTo("150");
    }

    @Test
    void lists_allocations_of_a_unit_account() {
        UnitAccountId unitAccountId = UnitAccountId.newId();
        adapter.save(UnitAccountAllocation.create(UnitAccountAllocationId.newId(), unitAccountId,
                UnitAccountMovementId.newId(), UnitAccountMovementId.newId(), Amount.of(new BigDecimal("100")),
                LocalDate.of(2026, 1, 5), EntityId.newId()));
        adapter.save(UnitAccountAllocation.create(UnitAccountAllocationId.newId(), unitAccountId,
                UnitAccountMovementId.newId(), UnitAccountMovementId.newId(), Amount.of(new BigDecimal("200")),
                LocalDate.of(2026, 1, 10), EntityId.newId()));
        adapter.save(UnitAccountAllocation.create(UnitAccountAllocationId.newId(), UnitAccountId.newId(),
                UnitAccountMovementId.newId(), UnitAccountMovementId.newId(), Amount.of(new BigDecimal("300")),
                LocalDate.of(2026, 1, 12), EntityId.newId()));

        assertThat(adapter.findAllByUnitAccountId(unitAccountId)).hasSize(2);
    }
}
