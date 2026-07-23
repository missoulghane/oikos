package com.architek.oikos.accounting.application.port.in;

import java.util.Optional;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.query.GetMovementQuery;

/**
 * Non-throwing variant of GetMovementUseCase: returns empty rather than
 * throwing when the movement does not exist. See FindAccountByHolderUseCase
 * for why a caller that must decide what to do with a "not found" outcome
 * (rather than letting the framework turn it into an HTTP error page)
 * should not rely on a throwing @Transactional use case from another module.
 */
public interface FindMovementUseCase {

    Optional<MovementView> findMovement(GetMovementQuery query);
}
