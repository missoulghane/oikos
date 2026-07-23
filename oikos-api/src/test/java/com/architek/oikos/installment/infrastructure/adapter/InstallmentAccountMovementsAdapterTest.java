package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.port.in.ListMovementsForAllocationUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;
import com.architek.oikos.installment.application.port.out.AccountMovement;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class InstallmentAccountMovementsAdapterTest {

    @Mock
    private ListMovementsForAllocationUseCase listMovementsForAllocationUseCase;

    @Test
    void keeps_only_credit_movements() {
        AccountId accountId = AccountId.newId();
        Instant occurredOn = Instant.parse("2027-01-01T00:00:00Z");
        MovementView credit = new MovementView(MovementId.newId(), accountId, occurredOn, MovementType.PAYMENT,
                MovementDirection.CREDIT, new BigDecimal("500"), "Paiement", null);
        MovementView debit = new MovementView(MovementId.newId(), accountId, occurredOn, MovementType.INSTALLMENT,
                MovementDirection.DEBIT, new BigDecimal("250"), "Appel", null);
        when(listMovementsForAllocationUseCase.listMovements(any())).thenReturn(List.of(credit, debit));

        List<AccountMovement> result = new InstallmentAccountMovementsAdapter(listMovementsForAllocationUseCase)
                .listCreditMovements(accountId.value());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(credit.id().value());
        assertThat(result.get(0).amount()).isEqualByComparingTo("500");
    }

    @Test
    void returns_an_empty_list_when_there_are_no_credit_movements() {
        EntityId accountId = EntityId.newId();
        when(listMovementsForAllocationUseCase.listMovements(any())).thenReturn(List.of());

        List<AccountMovement> result = new InstallmentAccountMovementsAdapter(listMovementsForAllocationUseCase)
                .listCreditMovements(accountId);

        assertThat(result).isEmpty();
    }
}
