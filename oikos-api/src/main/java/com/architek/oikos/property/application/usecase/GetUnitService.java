package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;

@Component
public class GetUnitService implements GetUnitUseCase {

    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;

    public GetUnitService(UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository) {
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UnitView getUnit(GetUnitQuery query) {
        Unit unit = unitRepository.findById(query.id())
                .orElseThrow(() -> new UnitNotFoundException(query.id()));
        boolean hasCoproprietaires = !unitOwnershipRepository.findAllByUnitId(unit.getId()).isEmpty();
        return UnitView.from(unit, hasCoproprietaires);
    }
}
