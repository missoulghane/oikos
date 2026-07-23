package com.architek.oikos.installment.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.in.FindMovementUseCase;
import com.architek.oikos.accounting.application.query.GetMovementQuery;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.installment.application.port.out.MovementInfo;
import com.architek.oikos.installment.application.port.out.MovementLookupPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: validates a movement referenced by a manual
 * allocation by delegating to accounting's public port-in (FindMovementUseCase),
 * never to accounting's repositories directly (rule 4). Uses the non-throwing
 * FindMovementUseCase rather than GetMovementUseCase so "not found" surfaces
 * as a normal empty Optional here, not a RuntimeException that would mark
 * the transaction rollback-only from within accounting's own @Transactional
 * service before AllocatePaymentService gets a chance to translate it into
 * its own domain exception.
 */
@Component
public class InstallmentMovementLookupAdapter implements MovementLookupPort {

    private final FindMovementUseCase findMovementUseCase;

    public InstallmentMovementLookupAdapter(FindMovementUseCase findMovementUseCase) {
        this.findMovementUseCase = findMovementUseCase;
    }

    @Override
    public Optional<MovementInfo> findMovement(EntityId movementId) {
        return findMovementUseCase.findMovement(new GetMovementQuery(MovementId.of(movementId.value())))
                .map(view -> new MovementInfo(view.accountId().value(), view.direction() == MovementDirection.CREDIT,
                        view.amount()));
    }
}
