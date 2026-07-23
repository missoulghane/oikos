package com.architek.oikos.accounting.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.port.in.FindMovementUseCase;
import com.architek.oikos.accounting.application.query.GetMovementQuery;
import com.architek.oikos.accounting.domain.repository.MovementRepository;

@Component
public class FindMovementService implements FindMovementUseCase {

    private final MovementRepository movementRepository;

    public FindMovementService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovementView> findMovement(GetMovementQuery query) {
        return movementRepository.findById(query.id()).map(MovementView::from);
    }
}
