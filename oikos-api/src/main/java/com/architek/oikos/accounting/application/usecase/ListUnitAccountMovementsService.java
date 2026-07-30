package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.UnitAccountMovementView;
import com.architek.oikos.accounting.application.port.in.ListUnitAccountMovementsUseCase;
import com.architek.oikos.accounting.application.query.ListUnitAccountMovementsQuery;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListUnitAccountMovementsService implements ListUnitAccountMovementsUseCase {

    private final UnitAccountRepository unitAccountRepository;
    private final UnitAccountMovementRepository unitAccountMovementRepository;

    public ListUnitAccountMovementsService(UnitAccountRepository unitAccountRepository,
                                            UnitAccountMovementRepository unitAccountMovementRepository) {
        this.unitAccountRepository = unitAccountRepository;
        this.unitAccountMovementRepository = unitAccountMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitAccountMovementView> list(ListUnitAccountMovementsQuery query) {
        UnitAccount unitAccount = unitAccountRepository.findByUnitId(query.unitId())
                .orElseThrow(() -> UnitAccountNotFoundException.forUnit(query.unitId()));
        return unitAccountMovementRepository.findPageByUnitAccountId(unitAccount.getId(), query.pageRequest())
                .map(UnitAccountMovementView::from);
    }
}
