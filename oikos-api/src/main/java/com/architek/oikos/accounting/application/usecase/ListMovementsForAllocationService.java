package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.port.in.ListMovementsForAllocationUseCase;
import com.architek.oikos.accounting.application.query.ListMovementsForAllocationQuery;
import com.architek.oikos.accounting.domain.repository.MovementRepository;

@Component
public class ListMovementsForAllocationService implements ListMovementsForAllocationUseCase {

    private final MovementRepository movementRepository;

    public ListMovementsForAllocationService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementView> listMovements(ListMovementsForAllocationQuery query) {
        return movementRepository.findAllByAccountId(query.accountId()).stream()
                .map(MovementView::from)
                .toList();
    }
}
