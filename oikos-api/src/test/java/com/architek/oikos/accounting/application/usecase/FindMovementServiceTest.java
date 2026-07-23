package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.query.GetMovementQuery;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

@ExtendWith(MockitoExtension.class)
class FindMovementServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Test
    void returns_the_movement_when_it_exists() {
        MovementId id = MovementId.newId();
        Movement movement = Movement.create(id, AccountId.newId(), Instant.parse("2027-01-01T00:00:00Z"),
                MovementType.PAYMENT, MovementDirection.CREDIT, com.architek.oikos.shared.domain.valueobject.Amount.of(new BigDecimal("500")),
                "Paiement", null);
        when(movementRepository.findById(id)).thenReturn(Optional.of(movement));

        var result = new FindMovementService(movementRepository).findMovement(new GetMovementQuery(id));

        assertThat(result).isPresent();
        assertThat(result.get().amount()).isEqualByComparingTo("500");
    }

    @Test
    void returns_empty_when_the_movement_does_not_exist() {
        MovementId id = MovementId.newId();
        when(movementRepository.findById(id)).thenReturn(Optional.empty());

        var result = new FindMovementService(movementRepository).findMovement(new GetMovementQuery(id));

        assertThat(result).isEmpty();
    }
}
