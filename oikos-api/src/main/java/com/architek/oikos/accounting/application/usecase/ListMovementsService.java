package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.application.port.in.ListMovementsUseCase;
import com.architek.oikos.accounting.application.query.ListMovementsQuery;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListMovementsService implements ListMovementsUseCase {

    private final MovementRepository movementRepository;

    public ListMovementsService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovementView> listMovements(ListMovementsQuery query) {
        return movementRepository.findPageByAccountId(query.accountId(), query.pageRequest()).map(MovementView::from);
    }
}
