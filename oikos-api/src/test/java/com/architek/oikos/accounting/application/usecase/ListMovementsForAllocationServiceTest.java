package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.query.ListMovementsForAllocationQuery;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

@ExtendWith(MockitoExtension.class)
class ListMovementsForAllocationServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Test
    void returns_every_movement_of_the_account_unpaginated() {
        AccountId accountId = AccountId.newId();
        Movement movement = Movement.create(MovementId.newId(), accountId, Instant.parse("2027-01-01T00:00:00Z"),
                MovementType.PAYMENT, MovementDirection.CREDIT, Amount.of(new BigDecimal("500")), "Paiement", null);
        when(movementRepository.findAllByAccountId(accountId)).thenReturn(List.of(movement));

        List<MovementView> views = new ListMovementsForAllocationService(movementRepository)
                .listMovements(new ListMovementsForAllocationQuery(accountId));

        assertThat(views).hasSize(1);
        assertThat(views.get(0).amount()).isEqualByComparingTo("500");
    }
}
