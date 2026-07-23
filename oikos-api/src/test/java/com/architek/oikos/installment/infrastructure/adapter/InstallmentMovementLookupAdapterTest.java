package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.port.in.FindMovementUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;
import com.architek.oikos.installment.application.port.out.MovementInfo;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class InstallmentMovementLookupAdapterTest {

    @Mock
    private FindMovementUseCase findMovementUseCase;

    private InstallmentMovementLookupAdapter newAdapter() {
        return new InstallmentMovementLookupAdapter(findMovementUseCase);
    }

    @Test
    void returns_the_movement_info_when_it_exists() {
        AccountId accountId = AccountId.newId();
        MovementId movementId = MovementId.newId();
        when(findMovementUseCase.findMovement(any())).thenReturn(Optional.of(new MovementView(movementId, accountId,
                Instant.parse("2027-01-01T00:00:00Z"), MovementType.PAYMENT, MovementDirection.CREDIT,
                new BigDecimal("500"), "Paiement", null)));

        var result = newAdapter().findMovement(movementId.value());

        assertThat(result).contains(new MovementInfo(accountId.value(), true, new BigDecimal("500")));
    }

    @Test
    void returns_empty_when_the_movement_does_not_exist() {
        EntityId movementId = EntityId.newId();
        when(findMovementUseCase.findMovement(any())).thenReturn(Optional.empty());

        var result = newAdapter().findMovement(movementId);

        assertThat(result).isEmpty();
    }
}
