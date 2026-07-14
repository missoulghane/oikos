package com.architek.oikos.property.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitOwnershipView;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.application.query.ListUnitOwnershipsByUnitQuery;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;

@Component
public class ListUnitOwnershipsByUnitService implements ListUnitOwnershipsByUnitUseCase {

    private final UnitOwnershipRepository unitOwnershipRepository;

    public ListUnitOwnershipsByUnitService(UnitOwnershipRepository unitOwnershipRepository) {
        this.unitOwnershipRepository = unitOwnershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOwnershipView> listUnitOwnerships(ListUnitOwnershipsByUnitQuery query) {
        return unitOwnershipRepository.findAllByUnitId(query.unitId()).stream().map(UnitOwnershipView::from).toList();
    }
}
