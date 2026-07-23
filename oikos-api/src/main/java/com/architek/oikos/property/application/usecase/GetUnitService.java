package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.query.GetUnitQuery;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;

@Component
public class GetUnitService implements GetUnitUseCase {

    private final UnitRepository unitRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public GetUnitService(UnitRepository unitRepository, UnitOwnershipRepository unitOwnershipRepository,
                           UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.unitRepository = unitRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UnitView getUnit(GetUnitQuery query) {
        Unit unit = unitRepository.findById(query.id())
                .orElseThrow(() -> new UnitNotFoundException(query.id()));
        boolean hasCoproprietaires = !unitOwnershipRepository.findAllByUnitId(unit.getId()).isEmpty();
        String unitTypeName = unitTypeDefinitionRepository.findById(unit.getUnitTypeId())
                .map(UnitTypeDefinition::getName)
                .orElse(null);
        return UnitView.from(unit, hasCoproprietaires, unitTypeName);
    }
}
