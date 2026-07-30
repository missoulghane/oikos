package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.UnitAccountSummaryView;
import com.architek.oikos.accounting.application.query.ListUnitAccountSummariesByPropertyQuery;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListUnitAccountSummariesByPropertyServiceTest {

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private UnitAccountMovementRepository unitAccountMovementRepository;

    private ListUnitAccountSummariesByPropertyService newService() {
        return new ListUnitAccountSummariesByPropertyService(unitAccountRepository, unitAccountMovementRepository);
    }

    @Test
    void summarizes_due_paid_advance_and_balance_per_unit() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        UnitAccountId unitAccountId = UnitAccountId.newId();
        UnitAccount account = UnitAccount.reconstruct(unitAccountId, unitId, propertyId, new BigDecimal("50"), Instant.now());

        when(unitAccountRepository.findAllByPropertyId(propertyId)).thenReturn(List.of(account));
        when(unitAccountMovementRepository.findAllByUnitAccountId(unitAccountId)).thenReturn(List.of(
                movement(unitAccountId, UnitAccountMovementType.FUND_CALL, UnitAccountMovementDirection.DEBIT, "200"),
                movement(unitAccountId, UnitAccountMovementType.PAYMENT, UnitAccountMovementDirection.CREDIT, "250")));

        List<UnitAccountSummaryView> summaries = newService()
                .list(new ListUnitAccountSummariesByPropertyQuery(propertyId));

        assertThat(summaries).hasSize(1);
        UnitAccountSummaryView summary = summaries.get(0);
        assertThat(summary.totalDue()).isEqualByComparingTo("200");
        assertThat(summary.totalPaid()).isEqualByComparingTo("250");
        assertThat(summary.currentBalance()).isEqualByComparingTo("50");
        assertThat(summary.availableAdvance()).isEqualByComparingTo("50");
    }

    private static UnitAccountMovement movement(UnitAccountId unitAccountId, UnitAccountMovementType type,
                                                 UnitAccountMovementDirection direction, String amount) {
        return UnitAccountMovement.create(UnitAccountMovementId.newId(), AccountingExerciseId.newId(), unitAccountId,
                LocalDate.now(), type, direction, Amount.of(new BigDecimal(amount)), null, "Mouvement", null);
    }
}
